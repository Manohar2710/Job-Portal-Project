# Logback Logging & Production Readiness Plan

## Overview

Configure production-ready logging for the `job-portal-service` multi-module Spring Boot 3.5 / Java 21 application using SLF4J + Logback (already bundled via `spring-boot-starter-web`).

**Goal:** Zero infrastructure changes required — the app already ships Logback. The plan adds:
1. A `logback-spring.xml` in the `job-service` module with profile-aware console appenders (human-readable colored text for `dev`, structured JSON for `prod`)
2. Structured `@Slf4j` log statements across all layers (service, filter, exception handler, controller) with proper masking of sensitive data
3. A request/response logging filter that logs full details only on `DEBUG` profile
4. Log-level tuning via `application.yaml`

**Non-goals:** File appenders, external log aggregation config, log shipping agents, or metrics changes.

---

## Sub-Task 1 — Logback Configuration File

### Intent
Create `logback-spring.xml` in `Springboot/job-service/src/main/resources/` that uses Spring Boot profile-aware `<springProfile>` blocks to switch between two console appenders:
- **`dev` profile:** Colored, human-readable pattern (matches current developer experience)
- **`prod` profile (default):** Structured JSON output (machine-parseable for ECS, K8s, CloudWatch, etc.)

The JSON pattern uses Logback's built-in `%replace` / encoder without extra libraries (no `logstash-logback-encoder` needed — keep dependencies minimal).

### Expected Outcomes
- Running with `-Dspring.profiles.active=dev` → colored, multi-line console log
- Running with `-Dspring.profiles.active=prod` (or no profile) → single-line JSON per log event
- Root log level: `INFO` by default; `DEBUG` available for the `debug` profile
- `com.learning` package always logs at `DEBUG`
- Hibernate SQL (`org.hibernate.SQL`) controlled by `show-sql` flag — set to `DEBUG` only in dev profile
- Framework noise (`org.springframework`, `io.jsonwebtoken`) capped at `WARN` in prod

### Todo List
1. Create `Springboot/job-service/src/main/resources/logback-spring.xml`
2. Define `CONSOLE_DEV` appender with `%clr` color pattern (timestamp, level, thread, logger, message, exception)
3. Define `CONSOLE_PROD` appender with JSON pattern (fields: `timestamp`, `level`, `thread`, `logger`, `message`, `exception`, `traceId` MDC slot)
4. Add `<springProfile name="dev">` block — root at `DEBUG`, activate `CONSOLE_DEV`, set `com.learning` to `DEBUG`, `org.hibernate.SQL` to `DEBUG`
5. Add `<springProfile name="prod | !dev">` block — root at `INFO`, activate `CONSOLE_PROD`, set `org.springframework` to `WARN`, `io.jsonwebtoken` to `WARN`
6. Add `<springProfile name="debug">` override block — root at `DEBUG`, full framework logging

### Relevant Context
- File location: `Springboot/job-service/src/main/resources/` (same directory as `application.yaml`)
- Spring Boot reads `logback-spring.xml` automatically; `<springProfile>` requires the `-spring` suffix
- No new Maven dependencies required — Logback Classic is on the classpath via `spring-boot-starter-web`

### Status
`[x] done`

---

## Sub-Task 2 — Log-Level Configuration in application.yaml

### Intent
Add a `logging:` block to `application.yaml` as a secondary safety net and to expose log level tuning to ops teams without touching XML. This also disables the verbose `show-sql: true` Hibernate flag in prod (replace with Logback-level control).

### Expected Outcomes
- `application.yaml` contains a `logging.level` section that mirrors the Logback XML defaults
- `spring.jpa.show-sql` is set to `false` (Logback handles SQL visibility via `org.hibernate.SQL` logger instead — avoids double-logging)
- Log config is visible at a glance without opening the XML file

### Todo List
1. Open `Springboot/job-service/src/main/resources/application.yaml`
2. Change `show-sql: true` → `show-sql: false` (Logback XML takes over SQL log control)
3. Add `logging:` section with `level.root: INFO`, `level.com.learning: DEBUG`, `level.org.springframework.web: WARN`, `level.org.hibernate.SQL: WARN`
4. Add a comment explaining that the Logback XML overrides these per profile for dev/prod

### Relevant Context
- File: `Springboot/job-service/src/main/resources/application.yaml`
- `show-sql: true` currently at line 15

### Status
`[x] done`

---

## Sub-Task 3 — Security Service Logging (AuthServiceImpl)

### Intent
Add `@Slf4j` log statements to `AuthServiceImpl` covering the full authentication lifecycle: registration attempt, duplicate email detection, user creation, login attempt, token issuance. Apply the agreed masking strategy: partial email masking, no passwords or tokens logged.

### Expected Outcomes
- `log.info` on registration start with masked email
- `log.warn` + message on duplicate email (before throwing `IllegalArgumentException`)
- `log.info` on successful user save + token issue (userId only, no token value)
- `log.info` on login attempt with masked email
- `log.warn` if post-auth user lookup fails (edge case `orElseThrow`)
- `log.info` on successful login + token issue

**Email masking helper** (inline, no utility class needed):
```
// input: manohar@example.com → output: m*****@example.com
private String maskEmail(String email) {
    int at = email.indexOf('@');
    return email.charAt(0) + "*".repeat(Math.max(1, at - 1)) + email.substring(at);
}
```

### Todo List
1. Open `Springboot/security-module/src/main/java/com/learning/security/service/impl/AuthServiceImpl.java`
2. Add `@Slf4j` Lombok annotation to the class
3. Add private `maskEmail(String)` helper method
4. Add `log.info("Register attempt for email: {}", maskEmail(...))` at start of `register()`
5. Add `log.warn("Registration rejected — email already exists: {}", maskEmail(...))` before `throw`
6. Add `log.info("User registered successfully, userId: {}", savedUser.getId())` after `userRepository.save()`
7. Add `log.info("Login attempt for email: {}", maskEmail(...))` at start of `login()`
8. Add `log.info("Login successful, userId: {}", user.getId())` after user lookup
9. Do NOT log the `accessToken` string anywhere

### Relevant Context
- File: `Springboot/security-module/src/main/java/com/learning/security/service/impl/AuthServiceImpl.java`
- `@RequiredArgsConstructor` is already present; add `@Slf4j` alongside it
- Lombok `@Slf4j` generates `private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);`

### Status
`[x] done`

---

## Sub-Task 4 — JWT Filter Logging (JwtAuthenticationFilter)

### Intent
Add structured log statements to `JwtAuthenticationFilter` to make JWT validation failures observable in production and auth success traceable for debugging.

### Expected Outcomes
- `log.debug` when no `Authorization` header is present (skip-filter path — debug only, too noisy for INFO)
- `log.debug` when token validated and SecurityContext populated (user + URI logged, no token value)
- `log.warn` when a `JwtException` is caught (exception message + request URI logged, no token value)

### Todo List
1. Open `Springboot/security-module/src/main/java/com/learning/security/filter/JwtAuthenticationFilter.java`
2. Add `@Slf4j` annotation to the class
3. Add `log.debug("No Authorization header on request: {}", request.getRequestURI())` in the skip-filter branch
4. Add `log.debug("JWT validated for user: {}, URI: {}", username, request.getRequestURI())` after SecurityContext is set
5. Add `log.warn("Invalid JWT token on request {}: {}", request.getRequestURI(), ex.getMessage())` in the `catch (JwtException)` block
6. Do NOT log the `jwt` token string

### Relevant Context
- File: `Springboot/security-module/src/main/java/com/learning/security/filter/JwtAuthenticationFilter.java`
- `@Component` and `@RequiredArgsConstructor` already present

### Status
`[x] done`

---

## Sub-Task 5 — Global Exception Handler Logging

### Intent
Add `log.error` / `log.warn` calls to `GlobalExceptionHandler` so that every unhandled exception surfaces in logs with enough context for post-mortem debugging, and the log entry is correlated to the HTTP response status.

### Expected Outcomes
- `log.warn` for 400/403/404 exceptions (client errors — not system failures)
- `log.error` with stack trace for any 5xx path (currently `IllegalArgumentException` is mapped to 500 by mistake — this is also a bug to note, not fix here)
- Each log entry includes the exception message

### Todo List
1. Open `Springboot/common-module/src/main/java/com/learning/common/exception/GlobalExceptionHandler.java`
2. Add `@Slf4j` annotation to the class
3. Add `log.warn("ResourceNotFoundException: {}", ex.getMessage())` in `handleException()`
4. Add `log.warn("Validation failed: {}", fieldErrors)` in `handleValidation()`
5. Add `log.warn("IllegalArgumentException: {}", exception.getMessage())` in `handleIllegalArgException()`
6. Add `log.warn("AccessDeniedException intercepted")` in `handleAccessDenied()`

### Relevant Context
- File: `Springboot/common-module/src/main/java/com/learning/common/exception/GlobalExceptionHandler.java`
- Note: `handleIllegalArgException` currently returns `HttpStatus.INTERNAL_SERVER_ERROR` in the body but uses `HttpStatus.BAD_REQUEST` for the HTTP status — this is a pre-existing inconsistency, do not fix it in this task

### Status
`[ ] pending`

---

## Sub-Task 6 — Service Layer Logging (JobService + AuthController)

### Intent
Add `log.info` / `log.debug` log statements to `JobService` for job lifecycle events, and `log.info` entry/exit statements to `AuthController` to bracket the HTTP auth endpoints.

### Expected Outcomes
- `JobService.create()` logs the job title and resulting job ID at `INFO` level
- `AuthController` logs register/login entry at `INFO` (no body content), exit at `INFO` with HTTP status
- No request body fields logged in controller (DTOs may contain passwords)

### Todo List
1. Open `Springboot/job-service/src/main/java/com/learning/job_portal_service/service/JobService.java`
2. Add `@Slf4j` to the class
3. Add `log.info("Creating job with title: {}", jobRequest.title())` at start of `create()`
4. Add `log.info("Job created successfully, jobId: {}", savedJob.getId())` after `jobRepository.save()`
5. Open `Springboot/security-module/src/main/java/com/learning/security/controller/AuthController.java`
6. Add `@Slf4j` to the class
7. Add `log.info("POST /register invoked")` at start of register endpoint
8. Add `log.info("POST /login invoked")` at start of login endpoint

### Relevant Context
- `JobService`: `Springboot/job-service/src/main/java/com/learning/job_portal_service/service/JobService.java`
- `AuthController`: `Springboot/security-module/src/main/java/com/learning/security/controller/AuthController.java`

### Status
`[ ] pending`

---

## Sub-Task 7 — Request/Response Logging Filter (DEBUG profile only)

### Intent
Create a `RequestLoggingFilter` bean in the `job-service` module that logs full HTTP request details (method, URI, headers excluding `Authorization`, body) and response status. This filter is **only active** on the `debug` Spring profile so it never runs in production.

Spring Boot provides `CommonsRequestLoggingFilter` out of the box — configure it as a `@Bean` with `@Profile("debug")` rather than writing a custom filter from scratch.

### Expected Outcomes
- On `debug` profile: every request logs method + URI + headers (minus Authorization) + query string + payload (up to 10 KB)
- On `prod`/`dev` profile: this bean is not registered, zero overhead
- Bean lives in `Springboot/job-service/src/main/java/com/learning/job_portal_service/config/` alongside existing config classes

### Todo List
1. Create `Springboot/job-service/src/main/java/com/learning/job_portal_service/config/RequestLoggingConfig.java`
2. Annotate class with `@Configuration` and `@Profile("debug")`
3. Define `@Bean CommonsRequestLoggingFilter requestLoggingFilter()` method
4. Configure: `setIncludeQueryString(true)`, `setIncludePayload(true)`, `setMaxPayloadLength(10240)`, `setIncludeHeaders(true)`, `setHeaderPredicate(name -> !name.equalsIgnoreCase("authorization"))`, `setAfterMessagePrefix("RESPONSE: ")`
5. Add a `logging.level.org.springframework.web.filter.CommonsRequestLoggingFilter: DEBUG` entry to `application.yaml` (gated under a `debug` profile-specific config block or a comment)

### Relevant Context
- `CommonsRequestLoggingFilter` is in `spring-webmvc`, already on the classpath
- Existing config classes: `Springboot/job-service/src/main/java/com/learning/job_portal_service/config/OpenApiConfig.java`, `PersistenceConfig.java`
- `@Profile("debug")` ensures zero production overhead

### Status
`[ ] pending`

---

## Execution Order

```
Sub-Task 1 → Sub-Task 2 → Sub-Task 3 → Sub-Task 4 → Sub-Task 5 → Sub-Task 6 → Sub-Task 7
```

Each sub-task is independently reviewable and should be committed separately.
