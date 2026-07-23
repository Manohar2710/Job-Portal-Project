# Spring Boot Advanced Guide
## Security, Caching, Async Processing, APIs, and Production Readiness

This guide continues from the foundational learning plan and focuses on the advanced backend capabilities expected from a strong mid-level to senior Spring Boot engineer.

You should study this guide after you are comfortable with:
- layered architecture
- DTO-based CRUD APIs
- validation and exception handling
- JPA basics
- testing fundamentals
- PostgreSQL and Flyway

This guide covers:
- authentication and authorization
- JWT-based security
- method-level access control
- caching with Redis
- async processing and schedulers
- event-driven patterns
- API documentation and versioning
- observability and production hardening

---

# 1. Advanced Learning Objectives

By the end of this guide, you should be able to:

- secure a Spring Boot API using JWT
- implement role-based and permission-based authorization
- design secure authentication flows
- use Redis for caching and token/session-related use cases
- implement async workflows safely
- schedule background jobs
- publish and consume domain events
- document APIs professionally
- version APIs safely
- add logging, metrics, and health checks
- discuss production trade-offs in interviews

---

# 2. Phase 3: Spring Security Fundamentals

Security is one of the most common differentiators between “can build CRUD” and “can build production systems”.

## 2.1 Core concepts to understand
- authentication: who are you?
- authorization: what are you allowed to do?
- principal: the authenticated identity
- credentials: password/token/etc.
- roles vs permissions
- stateless vs stateful authentication

## 2.2 Recommended approach for this project
Use:
- Spring Security
- JWT access tokens
- BCrypt password hashing
- stateless authentication
- role-based authorization

## 2.3 Typical auth flow
1. User submits email/password
2. Backend validates credentials
3. Backend generates JWT
4. Frontend stores token safely
5. Frontend sends token in `Authorization: Bearer <token>`
6. Backend validates token on each request
7. Security context is populated
8. Authorization rules are applied

---

# 3. Security Configuration

## 3.1 Security filter chain example

```java
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/jobs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/jobs/**").hasAnyRole("EMPLOYER", "ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

## Why stateless auth
Stateless JWT-based auth scales well because the server does not need to keep per-user session state in memory for each request.

## Trade-off
JWT revocation is harder than server-side sessions. You need strategies such as:
- short-lived access tokens
- refresh tokens
- token blacklist
- versioned tokens
- logout invalidation via Redis

---

# 4. User Model and Roles

## Example entities

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;
}
```

```java
public enum Role {
    CANDIDATE,
    EMPLOYER,
    ADMIN
}
```

## Senior design note
Start with a simple enum role model if requirements are small. Move to role-permission tables when:
- permissions become dynamic
- admins manage permissions
- multiple roles per user are needed
- enterprise authorization grows

---

# 5. Authentication Implementation

## 5.1 Password hashing

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Never store plain text passwords. Never use reversible encryption for passwords.

## 5.2 Login request/response

```java
public record LoginRequest(
    @Email @NotBlank String email,
    @NotBlank String password
) {}
```

```java
public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {}
```

## 5.3 Authentication service example

```java
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, "Bearer", 3600);
    }
}
```

---

# 6. JWT Service

## Example JWT service responsibilities
- generate token
- extract username
- validate token
- check expiration
- optionally include roles/claims

```java
@Service
public class JwtService {

    private final String secret = "replace-with-secure-env-based-secret-key";
    private final long expirationMs = 3600000;

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("role", user.getRole().name())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

## Important production note
Do not hardcode secrets. Use environment variables or secret managers.

---

# 7. JWT Filter

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

# 8. Method-Level Authorization

URL rules are not enough. Protect business operations too.

## Example

```java
@Service
public class JobManagementService {

    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    public JobResponse createJob(JobRequest request) {
        // implementation
        return null;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAnyJob(Long jobId) {
        // implementation
    }
}
```

## Why this matters
If controller mappings change or internal service methods are reused, method-level security still protects critical operations.

---

# 9. Refresh Tokens and Logout Strategy

Access tokens should be short-lived.

## Recommended pattern
- access token: short-lived, e.g. 15 minutes
- refresh token: longer-lived, e.g. 7 days
- store refresh token securely
- rotate refresh tokens on use
- revoke on logout or suspicious activity

## Storage options
- database
- Redis
- signed opaque token strategy

## Interview discussion point
Be ready to explain why refresh token rotation reduces replay risk.

---

# 10. Caching with Redis

Caching improves performance, but only when used intentionally.

## Good caching candidates
- frequently read reference data
- expensive computed responses
- dashboard summaries
- search results with stable filters
- token blacklist / session metadata

## Bad caching candidates
- highly volatile data without invalidation strategy
- security-sensitive data without careful design
- data where stale reads are unacceptable

## Enable caching

```java
@Configuration
@EnableCaching
public class CacheConfig {
}
```

## Example service caching

```java
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Cacheable(value = "companyById", key = "#id")
    @Transactional(readOnly = true)
    public CompanyResponse getById(Long id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        return map(company);
    }

    @CacheEvict(value = "companyById", key = "#id")
    public void evictCompanyCache(Long id) {
    }
}
```

## Senior considerations
- define TTLs
- know invalidation triggers
- monitor hit ratio
- avoid caching broken/null states unless intentional
- understand stale data trade-offs

---

# 11. Redis Configuration Example

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    type: redis
```

## Common Redis use cases in this project
- cache job search metadata
- store OTP or password reset tokens
- maintain token blacklist for logout
- rate limiting counters
- temporary workflow state

---

# 12. Async Processing

Not every task should block the request thread.

## Good async candidates
- sending emails
- generating reports
- audit logging
- notification fan-out
- file processing
- analytics events

## Enable async

```java
@Configuration
@EnableAsync
public class AsyncConfig {
}
```

## Example async service

```java
@Service
public class NotificationService {

    @Async
    public CompletableFuture<Void> sendApplicationSubmittedEmail(String email, String jobTitle) {
        // send email
        return CompletableFuture.completedFuture(null);
    }
}
```

## Important warning
`@Async` is not magic. You must think about:
- thread pool sizing
- retries
- failure handling
- idempotency
- tracing across async boundaries

---

# 13. Custom Executor Configuration

Default executors are rarely enough for production.

```java
@Configuration
@EnableAsync
public class AsyncExecutorConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-");
        executor.initialize();
        return executor;
    }
}
```

## Why configure this
Without explicit configuration, async workloads can become unpredictable under load.

---

# 14. Scheduled Jobs

Schedulers are useful for recurring maintenance and business workflows.

## Examples
- expire stale job postings
- send reminder emails
- clean temporary tokens
- aggregate daily metrics
- retry failed notifications

## Enable scheduling

```java
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
```

## Example scheduled task

```java
@Component
@RequiredArgsConstructor
public class JobMaintenanceScheduler {

    private final JobRepository jobRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void expireOldJobs() {
        jobRepository.expireJobsOlderThan(LocalDate.now().minusDays(30));
    }
}
```

## Senior considerations
- make scheduled jobs idempotent
- avoid overlapping runs
- log execution metrics
- think about distributed locking in multi-instance deployments

---

# 15. Event-Driven Patterns

You do not need Kafka to learn event-driven thinking.

## Example domain event
When a candidate applies for a job:
- save application
- publish `ApplicationSubmittedEvent`
- listener sends email
- listener updates analytics
- listener triggers notification

## Event class

```java
public record ApplicationSubmittedEvent(
    Long applicationId,
    Long candidateId,
    Long jobId,
    String candidateEmail
) {}
```

## Publish event

```java
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationEventPublisher eventPublisher;

    public void submitApplication(Long jobId, Long candidateId) {
        // save application
        eventPublisher.publishEvent(
            new ApplicationSubmittedEvent(1L, candidateId, jobId, "candidate@example.com")
        );
    }
}
```

## Listen for event

```java
@Component
public class ApplicationEventListener {

    @EventListener
    public void handleApplicationSubmitted(ApplicationSubmittedEvent event) {
        // send email, analytics, etc.
    }
}
```

## Why this matters
Events reduce tight coupling between workflows and side effects.

---

# 16. Transactional Events

Sometimes you only want side effects after a successful commit.

## Example

```java
@Component
public class ApplicationTransactionalListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        // safe to trigger external side effects now
    }
}
```

## Why use `AFTER_COMMIT`
If the transaction rolls back, you do not want to send emails or publish downstream actions for data that never persisted.

---

# 17. File Uploads and External Storage

A job portal often needs resume uploads.

## Basic controller example

```java
@PostMapping("/api/resumes")
public ResponseEntity<Void> uploadResume(@RequestParam("file") MultipartFile file) {
    // validate content type, size, scan if needed, store metadata
    return ResponseEntity.accepted().build();
}
```

## Senior considerations
- validate file size and type
- avoid storing large files in DB unless justified
- prefer object storage for scale
- scan for malware in sensitive systems
- store metadata separately
- use signed URLs when appropriate

---

# 18. API Documentation with OpenAPI / Swagger

Professional APIs should be discoverable.

## Benefits
- easier frontend integration
- faster onboarding
- clearer contracts
- better testing support

## Example annotations

```java
@Operation(summary = "Create a new job")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Job created"),
    @ApiResponse(responseCode = "400", description = "Validation failed"),
    @ApiResponse(responseCode = "403", description = "Forbidden")
})
@PostMapping("/api/jobs")
public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(jobService.create(request));
}
```

## Document:
- auth requirements
- request/response examples
- error responses
- pagination parameters
- enum meanings

---

# 19. API Versioning

Versioning is about change management.

## Common strategies
- URI versioning: `/api/v1/jobs`
- header versioning
- media type versioning

## Recommendation for learning project
Use URI versioning first because it is simple and explicit.

## Example
```text
/api/v1/jobs
/api/v2/jobs
```

## When to version
Version when you introduce breaking changes:
- response shape changes
- required fields change
- semantics change
- removed fields/endpoints

---

# 20. Logging Best Practices

Logs should help you debug production issues without leaking secrets.

## Good logging principles
- use structured logs where possible
- include correlation/request IDs
- log business milestones
- log failures with context
- never log passwords or tokens
- avoid noisy logs in hot paths

## Example

```java
@Slf4j
@Service
public class JobService {

    public void publishJob(Long jobId) {
        log.info("Publishing job with id={}", jobId);
        // implementation
    }
}
```

## Log levels
- `ERROR`: failures needing attention
- `WARN`: suspicious but recoverable
- `INFO`: important lifecycle/business events
- `DEBUG`: development diagnostics

---

# 21. Observability and Actuator

Production systems need visibility.

## Add Spring Boot Actuator
Useful endpoints:
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

## Example config

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when_authorized
```

## What to monitor
- request latency
- error rate
- DB connection pool usage
- cache hit/miss ratio
- JVM memory
- thread pool saturation
- scheduler failures

---

# 22. Rate Limiting and Abuse Protection

Public APIs need protection.

## Use cases
- login brute-force prevention
- OTP endpoint protection
- search endpoint abuse control
- file upload throttling

## Implementation options
- API gateway
- Redis-based counters
- Bucket4j in application layer

## Interview angle
Be ready to explain why rate limiting belongs at multiple layers in serious systems.

---

# 23. Performance Tuning Topics

## Database
- add indexes for common filters
- inspect slow queries
- avoid N+1 queries
- use projections when full entities are unnecessary

## Application
- cache expensive reads
- tune thread pools
- avoid blocking operations in request path
- paginate large result sets

## Serialization
- avoid huge nested responses
- use DTOs
- control lazy loading carefully

---

# 24. Common Advanced Pitfalls

- trusting JWT without validating signature/expiration
- storing secrets in source code
- using broad `permitAll`
- forgetting method-level authorization
- caching without invalidation strategy
- async methods calling themselves internally and expecting proxy behavior
- scheduled jobs duplicating work across instances
- publishing side effects before transaction commit
- logging sensitive data
- exposing actuator endpoints too broadly

---

# 25. Practice Exercises

## Exercise 1: JWT Authentication
Implement:
- register
- login
- protected endpoint
- role-based access for employer/admin

## Exercise 2: Refresh Token Flow
Implement:
- refresh token entity/store
- refresh endpoint
- logout invalidation

## Exercise 3: Redis Cache
Cache:
- company details
- job detail lookup
- dashboard summary

Measure:
- before/after response time
- cache hit ratio

## Exercise 4: Async Notifications
When application is submitted:
- persist application
- publish event
- send email asynchronously
- log failures

## Exercise 5: Scheduled Cleanup
Create a scheduled job to:
- remove expired reset tokens
- archive old jobs
- log execution summary

## Exercise 6: OpenAPI Documentation
Document:
- auth endpoints
- job endpoints
- error responses
- pagination parameters

---

# 26. Senior Interview Questions

1. How does Spring Security filter chain work?
2. Why choose JWT over session-based auth?
3. What are the trade-offs of stateless authentication?
4. How do you revoke JWTs?
5. When should you use `@PreAuthorize`?
6. What are common Redis use cases in backend systems?
7. How do you handle cache invalidation?
8. What are the risks of async processing?
9. How do scheduled jobs behave in horizontally scaled systems?
10. What is the difference between `@EventListener` and `@TransactionalEventListener`?
11. How do you version APIs safely?
12. What metrics would you monitor in production?
13. How do you prevent brute-force login attacks?
14. How do you secure actuator endpoints?
15. How do you debug intermittent production failures?

---

# 27. Production Readiness Checklist for Backend

Before calling your backend “advanced”, verify:

- authentication works correctly
- authorization rules are tested
- secrets are externalized
- migrations are versioned
- logs are meaningful
- health checks are exposed safely
- metrics are available
- cache behavior is understood
- async tasks are monitored
- scheduled jobs are idempotent
- API docs are current
- error responses are consistent
- critical flows have integration tests

---

# 28. What to Study Next

After this guide:
1. Move to `ANGULAR-ADVANCED-GUIDE.md`
2. Integrate secure frontend auth flow
3. Add interceptors and guards
4. Practice full-stack debugging
5. Start mock interviews with `INTERVIEW-PREPARATION-GUIDE.md`

---

# 29. Final Advice

Advanced backend engineering is not about adding more annotations.

It is about understanding:
- trust boundaries
- failure modes
- consistency
- performance bottlenecks
- operational visibility
- safe evolution over time

If you can explain not only how something works, but also when it fails and what trade-offs it introduces, you are moving toward senior-level backend thinking.