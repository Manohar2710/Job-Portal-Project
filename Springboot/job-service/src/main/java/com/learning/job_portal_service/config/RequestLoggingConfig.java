package com.learning.job_portal_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Registers a {@link CommonsRequestLoggingFilter} that logs the full details of every
 * inbound HTTP request — method, URI, query string, headers (Authorization excluded),
 * and request body (up to 10 KB).
 *
 * <p><strong>Active only on the {@code debug} Spring profile.</strong>
 * The bean is never instantiated on {@code dev} or {@code prod}, so there is zero
 * overhead in normal operation.</p>
 *
 * <p>The filter output is controlled by the logger
 * {@code org.springframework.web.filter.CommonsRequestLoggingFilter}, which is set
 * to {@code DEBUG} in the {@code debug} profile block of {@code logback-spring.xml}.</p>
 *
 * <p>To activate, start the application with:</p>
 * <pre>
 *   -Dspring.profiles.active=debug
 * </pre>
 */
@Configuration
@Profile("debug")
public class RequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();

        // Include the query string in the logged URI (e.g. /api/jobs?status=OPEN)
        filter.setIncludeQueryString(true);

        // Log request headers — Authorization header is excluded via the predicate below
        filter.setIncludeHeaders(true);
        filter.setHeaderPredicate(name -> !name.equalsIgnoreCase("Authorization"));

        // Log the request body (buffered in memory up to maxPayloadLength bytes)
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10_240); // 10 KB cap — prevents log flooding on large uploads

        // Prefix printed before the log line for easy grep
        filter.setBeforeMessagePrefix("REQUEST  : ");
        filter.setAfterMessagePrefix("REQUEST  : ");

        return filter;
    }
}
