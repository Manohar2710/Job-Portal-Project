package com.learning.application_service.config;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Secondary datasource configuration — MySQL.
 *
 * Owns:
 *   - com.learning.application_service.audit.entity  (ApplicationAuditLog)
 *
 * Repositories:
 *   - com.learning.application_service.audit.repository
 *
 * YAML binding: app.datasource.secondary.*
 * (Custom root prefix avoids the Spring YAML sibling-key collision under spring.datasource)
 *
 * Use @Transactional("secondaryTransactionManager") to write to this datasource.
 *
 * NOTE: The EntityManagerFactory is built directly with LocalContainerEntityManagerFactoryBean
 * (NOT via the shared EntityManagerFactoryBuilder from PrimaryDataSourceConfig). This is
 * necessary because the shared builder carries PostgreSQL-scoped properties from spring.jpa.*,
 * and Hibernate cannot auto-detect the MySQL dialect through it.
 * The MySQL dialect is set explicitly here so no JDBC metadata lookup is needed at startup.
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.learning.application_service.audit.repository",
    entityManagerFactoryRef = "secondaryEntityManagerFactory",
    transactionManagerRef   = "secondaryTransactionManager"
)
public class SecondaryDataSourceConfig {

    // ── DataSource ────────────────────────────────────────────────────────────

    /**
     * Binds app.datasource.secondary.* from application.yaml.
     * Custom prefix avoids YAML nesting conflict under spring.datasource.
     */
    @Bean
    @ConfigurationProperties("app.datasource.secondary")
    public DataSourceProperties secondaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource secondaryDataSource() {
        return secondaryDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    // ── EntityManagerFactory ──────────────────────────────────────────────────

    /**
     * Built directly — NOT via the shared EntityManagerFactoryBuilder.
     *
     * The shared builder was constructed with PostgreSQL-scoped JPA properties
     * (spring.jpa.*). Passing the MySQL DataSource through it causes Hibernate to
     * fail to auto-detect the dialect from JDBC metadata.
     *
     * Solution: create a fresh LocalContainerEntityManagerFactoryBean with a new
     * HibernateJpaVendorAdapter and explicitly set the MySQL dialect + ddl-auto.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
            @Qualifier("secondaryDataSource") DataSource dataSource) {

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.learning.application_service.audit.entity");
        emf.setPersistenceUnitName("secondary");
        emf.setJpaVendorAdapter(vendorAdapter);

        // allow_jdbc_metadata_access=false: Hibernate skips any JDBC connection at
        // boot for dialect/environment resolution — required because MySQL may not be
        // running at startup.
        // ddl-auto=none: schema management for the optional audit DB is left to the
        // DBA / manual DDL; Hibernate will not attempt to open a connection for DDL.
        emf.setJpaPropertyMap(Map.of(
            "hibernate.dialect",                          "org.hibernate.dialect.MySQLDialect",
            "hibernate.boot.allow_jdbc_metadata_access",  "false",
            "hibernate.hbm2ddl.auto",                     "none",
            "hibernate.show_sql",                         "false"
        ));

        return emf;
    }

    // ── TransactionManager ────────────────────────────────────────────────────

    @Bean
    public PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

}
