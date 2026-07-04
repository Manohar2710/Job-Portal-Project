package com.learning.job_portal_service.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {
    "com.learning.job_portal_service.entity",
    "com.learning.security.entity"
})
@EnableJpaRepositories(basePackages = {
    "com.learning.job_portal_service.repository",
    "com.learning.security.repository"
})
public class PersistenceConfig {

}
