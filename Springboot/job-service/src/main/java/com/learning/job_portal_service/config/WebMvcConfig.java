package com.learning.job_portal_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.learning.job_portal_service.interceptor.RequestTracingInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements  WebMvcConfigurer {
        private final RequestTracingInterceptor requestTracingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestTracingInterceptor)
                .addPathPatterns("/api/**")              // all business endpoints
                .excludePathPatterns(
                    "/actuator/**",                      // health / metrics — no tracing noise
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                );
    }
}
