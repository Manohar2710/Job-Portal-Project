package com.learning.application_service.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

/**
 * Primary datasource configuration — PostgreSQL.
 *
 * Owns:
 *   - com.learning.application_service.entity  (applications, resumes, notes)
 *   - com.learning.security.entity             (users, roles, refresh_tokens)
 *
 * Repositories:
 *   - com.learning.application_service.repository
 *   - com.learning.security.repository
 *
 * Flyway migrations: classpath:db/app-migration  (PostgreSQL DDL)
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties({ JpaProperties.class, HibernateProperties.class })
@EnableJpaRepositories(
    basePackages = {
        "com.learning.application_service.repository",
        "com.learning.security.repository"
    },
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef   = "primaryTransactionManager"
)
public class PrimaryDataSourceConfig {

    // ── DataSource ────────────────────────────────────────────────────────────

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    // ── EntityManagerFactoryBuilder ───────────────────────────────────────────

    /**
     * Manually define EntityManagerFactoryBuilder because HibernateJpaAutoConfiguration
     * (which normally produces it) is excluded in application.yaml.
     *
     * JpaProperties + HibernateProperties supply ddl-auto, naming strategy, etc.
     * from spring.jpa.* in application.yaml.
     */
    @Primary
    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(
            JpaProperties jpaProperties,
            HibernateProperties hibernateProperties,
            ObjectProvider<HibernateSettings> hibernateSettingsProvider,
            ObjectProvider<PersistenceUnitManager> persistenceUnitManager) {

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        // Build properties supplier: called per-DataSource like Spring Boot internals do
        Map<String, Object> baseProps = new HashMap<>(
                hibernateProperties.determineHibernateProperties(
                        jpaProperties.getProperties(),
                        hibernateSettingsProvider.getIfAvailable(HibernateSettings::new)
                )
        );

        // EntityManagerFactoryBuilder(JpaVendorAdapter, Function<DataSource,Map>, PersistenceUnitManager)
        return new EntityManagerFactoryBuilder(
                vendorAdapter,
                ds -> baseProps,
                persistenceUnitManager.getIfAvailable()
        );
    }

    // ── Flyway ────────────────────────────────────────────────────────────────

    /**
     * Runs ALL migrations needed by this service on the primary (PostgreSQL) datasource:
     *
     *   classpath:db/migration      — security-module migrations (V2: users/roles/refresh_tokens,
     *                                 V3: seed roles). These are bundled inside security-module.jar
     *                                 and must run here because application_db is a fresh database
     *                                 that does not inherit tables from any other service.
     *
     *   classpath:db/app-migration  — application-service migrations (V1: applications/resumes/notes)
     *
     * IMPORTANT: primaryEntityManagerFactory uses @DependsOn("primaryFlyway") so Hibernate
     * schema validation only runs AFTER Flyway has created all tables.
     */
    @Bean
    public Flyway primaryFlyway() {
        Flyway flyway = Flyway.configure()
                .dataSource(primaryDataSource())
                .locations(
                    "classpath:db/migration",      // security-module: users, roles, refresh_tokens
                    "classpath:db/app-migration"   // application-service: applications, resumes, notes
                )
                .table("flyway_schema_history_application")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
        flyway.migrate();
        return flyway;
    }

    // ── EntityManagerFactory ──────────────────────────────────────────────────

    /**
     * @DependsOn("primaryFlyway") guarantees Flyway finishes creating all tables
     * BEFORE Hibernate runs ddl-auto validation. Without this, validation races
     * against migration and fails with "missing table".
     */
    @Primary
    @Bean
    @DependsOn("primaryFlyway")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(primaryDataSource())
                .packages(
                    "com.learning.application_service.entity",
                    "com.learning.security.entity"
                )
                .persistenceUnit("primary")
                .build();
    }

    // ── TransactionManager ────────────────────────────────────────────────────

    @Primary
    @Bean
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
