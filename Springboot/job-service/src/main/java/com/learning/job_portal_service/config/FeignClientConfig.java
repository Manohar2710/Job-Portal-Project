package com.learning.job_portal_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign request interceptor — propagates the inbound JWT Bearer token to all
 * outgoing Feign calls made by this service.
 *
 * Without this, inter-service Feign requests arrive at downstream services
 * with no Authorization header and are rejected with HTTP 401.
 *
 * How it works:
 *   1. The user's request arrives at job-service carrying "Authorization: Bearer <token>".
 *   2. Spring stores the request in a thread-local via RequestContextHolder.
 *   3. Before every Feign call, this interceptor reads that header and copies it
 *      onto the outgoing request so notification-service sees a valid token.
 */
@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor jwtTokenPropagationInterceptor() {
        return new RequestInterceptor() {
            private static final String AUTHORIZATION = "Authorization";

            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attrs == null) {
                    return; // no active HTTP request (e.g. async thread) — skip
                }

                HttpServletRequest request = attrs.getRequest();
                String authHeader = request.getHeader(AUTHORIZATION);

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    template.header(AUTHORIZATION, authHeader);
                }
            }
        };
    }
}
