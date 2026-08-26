package com.learning.job_portal_service.interceptor;

import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.lang.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Injects a correlation request ID into MDC and logs a structured access-log
 * line (method | URI | status | duration | user) after every request completes.
 *
 * <p>
 * Flow:
 * <ol>
 * <li>{@code preHandle} — assign/propagate {@code X-Request-Id}, record start
 * time in MDC</li>
 * <li>{@code afterCompletion} — log the outcome, clean up MDC</li>
 * </ol>
 *
 * <p>
 * Why an interceptor and not a filter: the interceptor runs inside Spring MVC
 * after the JWT filter has already populated the SecurityContext, so we can log
 * the authenticated username without re-parsing the token.
 */
@Slf4j
@Component
public class RequestTracingInterceptor implements HandlerInterceptor {

    // Header name forwarded by the API Gateway (or generated here if absent)
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    // MDC keys — used in logback-spring.xml pattern (e.g. %X{requestId})
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_START_TIME = "startTime";
    public static final String MDC_USERNAME = "username";

    /**
     * Runs AFTER the JWT filter has authenticated the request, BEFORE the
     * controller.
     * Sets MDC keys that will appear in every log statement during this request.
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        // 1. Resolve or generate a request correlation ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_REQUEST_ID, requestId);

        // 2. Echo the request ID back to the caller so they can correlate logs
        response.setHeader(REQUEST_ID_HEADER, requestId);

        // 3. Record start time for latency calculation in afterCompletion
        MDC.put(MDC_START_TIME, String.valueOf(System.currentTimeMillis()));

        // 4. Stamp the authenticated user into MDC (JWT filter has already run)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            MDC.put(MDC_USERNAME, auth.getName());
        }

        log.debug("→ {} {} (requestId={})", request.getMethod(), request.getRequestURI(), requestId);
        return true; // proceed to controller
    }

    /**
     * Always runs after the response is committed (even on exception).
     * Logs the access-log line and clears MDC to prevent thread-pool leakage.
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception ex) {

        long durationMs = 0;
        String start = MDC.get(MDC_START_TIME);
        if (start != null) {
            durationMs = System.currentTimeMillis() - Long.parseLong(start);
        }

        String handlerName = (handler instanceof HandlerMethod hm)
                ? hm.getBeanType().getSimpleName() + "." + hm.getMethod().getName()
                : handler.getClass().getSimpleName();

        if (ex != null) {
            log.warn("← {} {} | handler={} | status={} | {}ms | EXCEPTION: {}",
                    request.getMethod(), request.getRequestURI(),
                    handlerName, response.getStatus(), durationMs, ex.getMessage());
        } else {
            log.info("← {} {} | handler={} | status={} | {}ms",
                    request.getMethod(), request.getRequestURI(),
                    handlerName, response.getStatus(), durationMs);
        }

        // Always clean up — critical on thread-pool-based servers (Tomcat)
        MDC.remove(MDC_REQUEST_ID);
        MDC.remove(MDC_START_TIME);
        MDC.remove(MDC_USERNAME);
    }
}
