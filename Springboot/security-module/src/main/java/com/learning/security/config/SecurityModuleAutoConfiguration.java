package com.learning.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan(basePackages = "com.learning.security")
@EntityScan(basePackages = "com.learning.security.entity")
@EnableJpaRepositories(basePackages = "com.learning.security.repository")
public class SecurityModuleAutoConfiguration {

}
