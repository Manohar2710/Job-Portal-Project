# Spring Boot Security Topics Guide — Plan

## Top-Level Overview

**Goal:** Produce a comprehensive, single-source `SPRING-BOOT-SECURITY-GUIDE.md` that covers every important Spring Security topic for both junior and senior developers. The guide is grounded in the existing `security-module` implementation in this repo, and extends it with the topics that are currently absent from the existing guides.

**Scope:**
- Covers topics from "what is Spring Security" (junior) through "OAuth2, reactive security, rate limiting, audit logging, supply chain security" (senior)
- Every section ties theory → code pattern → interview angle → trade-offs
- No duplicate of topics already implemented in the existing codebase — instead, the guide references the actual files in `Springboot/security-module/`
- Output: one new file — `SPRING-BOOT-SECURITY-GUIDE.md`

**What already exists (do NOT re-explain from scratch):**
- `SecurityConfig.java` — filter chain, CORS, CSRF, stateless session, route rules, `@EnableMethodSecurity`
- `JwtAuthenticationFilter.java` — `OncePerRequestFilter` JWT extraction flow
- `JwtServiceImpl.java` — token generation, validation, claims extraction
- `AuthServiceImpl.java` — register, login, refresh, logout flows
- `RefreshTokenServiceImpl.java` — rotation, revocation, `SecureRandom` generation
- `SPRING-BOOT-ADVANCED-GUIDE.md` — security fundamentals, method-level auth (sections 2–9)

---

## Sub-Tasks

---

### Sub-Task 1 — Core Spring Security Architecture (Junior + Senior)

**Status:** [ ] pending

**Intent:**
Establish the architectural foundation. Junior devs need to understand what Spring Security is and how its filter chain works. Senior devs need to articulate the internal pipeline, SecurityContext propagation, and how to extend or replace filters.

**Expected Outcomes:**
- Section written: "Spring Security Architecture"
- Covers: filter chain, SecurityContext / SecurityContextHolder, Authentication object, AuthenticationManager → AuthenticationProvider → UserDetailsService delegation chain, GrantedAuthority model
- Junior angle: what is each component?
- Senior angle: how are they wired, when would you replace the default provider, thread-local vs reactive context propagation

**Todo List:**
1. Write "What Spring Security Does" — the filter chain mental model
2. Write "SecurityContextHolder and SecurityContext" — how the authentication object is stored and thread-local implications
3. Write "AuthenticationManager → AuthenticationProvider → UserDetailsService" — full delegation chain with the DaoAuthenticationProvider example from this project
4. Write "GrantedAuthority vs Role" — the `ROLE_` prefix convention, hasRole vs hasAuthority difference
5. Add senior note: replacing DaoAuthenticationProvider with a custom provider (e.g., LDAP, OAuth2, SAML)
6. Add interview Q&A block

**Relevant Context:**
- [`SecurityConfig.java`](Springboot/security-module/src/main/java/com/learning/security/config/SecurityConfig.java) — `authenticationProvider()`, `authenticationManager()` beans
- [`UserDetailsServiceImpl.java`](Springboot/security-module/src/main/java/com/learning/security/service/impl/UserDetailsServiceImpl.java)
- `SPRING-BOOT-ADVANCED-GUIDE.md` sections 2–3

---

### Sub-Task 2 — Password Security (Junior + Senior)

**Status:** [ ] pending

**Intent:**
Explain password encoding in depth. This project uses BCrypt but developers need to understand why, and what else exists.

**Expected Outcomes:**
- Section written: "Password Security"
- Covers: BCrypt, SCrypt, Argon2, PBKDF2, DelegatingPasswordEncoder, why plain text / MD5 / SHA-1 are dangerous
- Junior angle: how to use `PasswordEncoder`
- Senior angle: password migration strategy with `DelegatingPasswordEncoder`, strength tuning, salting internals

**Todo List:**
1. Write BCrypt explanation — work factor, salt, why it resists brute force
2. Write `DelegatingPasswordEncoder` — how to support multiple algorithms simultaneously during migration
3. Write upgrade strategy — encoding ids in stored hash, transparent re-hashing on login
4. Add "what never to do" — plain text, reversible encryption, unsalted hashes
5. Add interview Q&A block

**Relevant Context:**
- [`SecurityConfig.java`](Springboot/security-module/src/main/java/com/learning/security/config/SecurityConfig.java:117) — `BCryptPasswordEncoder` bean
- [`AuthServiceImpl.java`](Springboot/security-module/src/main/java/com/learning/security/service/impl/AuthServiceImpl.java:63) — `passwordEncoder.encode()`

---

### Sub-Task 3 — JWT Deep Dive (Junior + Senior)

**Status:** [ ] pending

**Intent:**
Cover JWT end-to-end: structure, signing algorithms, claims design, token validation, expiry, revocation strategies. This is by far the most commonly tested security topic in interviews.

**Expected Outcomes:**
- Section written: "JWT Authentication Deep Dive"
- Covers: JWT structure (header.payload.signature), HS256 vs RS256 vs ES256, standard vs custom claims, token expiry, token revocation strategies (blacklist, version counter, Redis), the security risks of `alg: none` attacks
- Junior angle: how to generate and validate a token
- Senior angle: choosing RS256 for microservices, claim design, revocation patterns without invalidating statelessness

**Todo List:**
1. Write JWT structure — base64url encoded header, payload, signature
2. Write signing algorithms comparison — HS256 (shared secret), RS256 (public/private key), ES256 (ECDSA)
3. Write claims best practices — `sub`, `iat`, `exp`, `iss`, `aud`, custom `roles` claim
4. Write token expiry and refresh flow — reference existing `JwtServiceImpl` and `RefreshTokenServiceImpl`
5. Write revocation strategies — short TTL, refresh token rotation (already implemented), Redis blacklist, token version on user
6. Write common vulnerabilities — `alg: none`, weak secrets, leaking tokens in logs
7. Add interview Q&A block

**Relevant Context:**
- [`JwtServiceImpl.java`](Springboot/security-module/src/main/java/com/learning/security/service/impl/JwtServiceImpl.java)
- [`RefreshTokenServiceImpl.java`](Springboot/security-module/src/main/java/com/learning/security/service/impl/RefreshTokenServiceImpl.java)
- [`JwtAuthenticationFilter.java`](Springboot/security-module/src/main/java/com/learning/security/filter/JwtAuthenticationFilter.java)

---

### Sub-Task 4 — Authorization: RBAC, PBAC, and Method Security (Junior + Senior)

**Status:** [ ] pending

**Intent:**
Cover the full authorization layer: URL-level rules, method-level security, and the evolution from simple roles to permission-based access control.

**Expected Outcomes:**
- Section written: "Authorization — Roles, Permissions, and Method Security"
- Covers: URL-level `authorizeHttpRequests`, `@PreAuthorize`, `@PostAuthorize`, `@Secured`, `@RolesAllowed`, SpEL expressions, permission evaluators, RBAC vs PBAC
- Junior angle: how to use `@PreAuthorize` with roles
- Senior angle: custom `PermissionEvaluator` for ownership checks (e.g., can this employer edit only their own job?), moving to fine-grained permissions table

**Todo List:**
1. Write URL-level authorization — `requestMatchers`, `hasRole`, `hasAuthority`, `hasAnyRole`, `permitAll`
2. Write method-level security — `@PreAuthorize`, `@PostAuthorize`, SpEL expressions including `#username == authentication.name`
3. Write `@Secured` and `@RolesAllowed` — simpler alternatives and when to use them
4. Write custom `PermissionEvaluator` — for resource ownership checks
5. Write RBAC vs PBAC comparison — when to evolve from enum roles to permissions table
6. Add interview Q&A block

**Relevant Context:**
- [`SecurityConfig.java`](Springboot/security-module/src/main/java/com/learning/security/config/SecurityConfig.java:9) — `@EnableMethodSecurity`
- `SPRING-BOOT-ADVANCED-GUIDE.md` section 8

---

### Sub-Task 5 — CORS and CSRF (Junior + Senior)

**Status:** [ ] pending

**Intent:**
These two are heavily tested topics that are commonly misunderstood, especially in API-first projects. The project already disables CSRF and configures CORS — this section explains why and when those choices are correct.

**Expected Outcomes:**
- Section written: "CORS and CSRF"
- Covers: what CORS is, preflight requests, allowed origins/methods/headers, why CSRF is disabled for stateless JWT APIs, when CSRF IS still needed (cookie-based sessions), SameSite cookie attribute as an alternative
- Junior angle: what error does CORS produce and how to fix it
- Senior angle: why CSRF protection is tied to cookie-based auth, double-submit cookie pattern, SameSite=Strict

**Todo List:**
1. Write CORS explanation — same-origin policy, preflight, allowed origins
2. Write CORS configuration — reference existing `CorsConfig.java` and `CorsProperties.java`
3. Write CSRF explanation — what the attack is, what protects against it
4. Write "why disable CSRF for JWT APIs" — stateless bearer token cannot be silently sent by a forged request
5. Write "when NOT to disable CSRF" — if tokens are stored in cookies
6. Write SameSite cookie attribute — an alternative mitigation
7. Add interview Q&A block

**Relevant Context:**
- [`CorsConfig.java`](Springboot/security-module/src/main/java/com/learning/security/config/CorsConfig.java)
- [`CorsProperties.java`](Springboot/security-module/src/main/java/com/learning/security/config/CorsProperties.java)
- [`SecurityConfig.java`](Springboot/security-module/src/main/java/com/learning/security/config/SecurityConfig.java:43) — `csrf.disable()`

---

### Sub-Task 6 — OAuth2 and OpenID Connect (Senior)

**Status:** [ ] pending

**Intent:**
OAuth2 and OIDC are senior-level differentiators. The current project does not use them but understanding the flows, when to use them, and how to integrate them with Spring Security is critical for senior interviews and real-world systems.

**Expected Outcomes:**
- Section written: "OAuth2 and OpenID Connect"
- Covers: OAuth2 roles (resource owner, client, authorization server, resource server), authorization code flow, PKCE, client credentials flow, implicit flow (deprecated), OIDC on top of OAuth2, Spring Security's `spring-boot-starter-oauth2-resource-server` and `spring-boot-starter-oauth2-client`, JWT resource server configuration
- Junior angle: what is OAuth2 at a conceptual level
- Senior angle: when to use authorization code + PKCE vs client credentials, how to configure Spring Boot as a resource server validating JWTs from Keycloak/Auth0/Okta

**Todo List:**
1. Write OAuth2 roles and concepts — resource owner, client, authorization server, resource server
2. Write authorization code + PKCE flow — step by step with sequence diagram (text-based)
3. Write client credentials flow — service-to-service auth use case
4. Write OIDC extension — `id_token`, UserInfo endpoint, claims
5. Write Spring Boot resource server configuration — `spring-security-oauth2-resource-server`, JWT decoder, authority mapping from claims
6. Write "when to use an authorization server" — Keycloak, Auth0, Okta, or custom
7. Write social login (Google/GitHub) integration pattern using `oauth2-client`
8. Add interview Q&A block

**Relevant Context:**
- No existing code — conceptual + configuration guide with code snippets
- Links to existing `SecurityConfig.java` to show contrast with current JWT approach

---

### Sub-Task 7 — HTTPS, Secrets Management, and Transport Security (Junior + Senior)

**Status:** [ ] pending

**Intent:**
Production security goes beyond filters and tokens. This section covers transport-level security and secrets externalization — topics that are asked in every senior interview.

**Expected Outcomes:**
- Section written: "HTTPS, Secrets Management, and Transport Security"
- Covers: HTTPS enforcement in Spring Boot, HSTS, externalizing secrets (environment variables, Spring Cloud Config, AWS Secrets Manager, Vault), never hardcoding secrets in code or `application.yml`
- Junior angle: how to run Spring Boot with SSL, what environment variables look like
- Senior angle: Vault integration pattern, secret rotation without restarts, 12-factor app principles, HSTS header

**Todo List:**
1. Write HTTPS in Spring Boot — `server.ssl.*` properties, self-signed vs CA cert
2. Write HSTS — `Strict-Transport-Security` header, Spring Security's `headers().httpStrictTransportSecurity()`
3. Write secrets externalization — environment variables, Spring Cloud Config, HashiCorp Vault
4. Write "what never to do" — secrets in source code, application.yml committed to git, logs that print tokens
5. Write Spring Boot + Vault quick reference — `spring-cloud-vault` auto-config pattern
6. Reference `JwtProperties.java` as an example of externalizing JWT secret via config binding
7. Add interview Q&A block

**Relevant Context:**
- [`JwtProperties.java`](Springboot/security-module/src/main/java/com/learning/security/config/JwtProperties.java) — `@ConfigurationProperties` pattern
- `SPRING-BOOT-ADVANCED-GUIDE.md` section 6 (important production note on secrets)

---

### Sub-Task 8 — Rate Limiting, Brute Force Protection, and Account Lockout (Senior)

**Status:** [ ] pending

**Intent:**
Auth endpoints are the most attacked endpoints in any system. This section covers how to protect them beyond just validating tokens.

**Expected Outcomes:**
- Section written: "Rate Limiting, Brute Force Protection, and Account Lockout"
- Covers: login endpoint rate limiting (Bucket4j, Redis-based rate limiter), failed login tracking, account lockout after N failures, CAPTCHA integration points, IP-based throttling, Spring Security's `AbstractUserDetailsAuthenticationProvider` lockout hooks
- Junior angle: why rate limiting on login matters
- Senior angle: distributed rate limiting with Redis, `UserDetails.isAccountNonLocked()`, unlock strategies (time-based vs admin-reset), DoS vs credential stuffing distinction

**Todo List:**
1. Write why auth endpoints need special protection — credential stuffing, brute force, spraying
2. Write Bucket4j rate limiting — token bucket algorithm, in-memory vs Redis-backed
3. Write failed login counter pattern — tracking in DB, `UserDetails.isAccountNonLocked()`
4. Write account lockout implementation — Spring Security hooks, `AccountStatusUserDetailsChecker`
5. Write unlock strategies — time-based auto-unlock vs admin action
6. Write IP-based throttling — using `HandlerInterceptor` or a custom filter
7. Add interview Q&A block

**Relevant Context:**
- `SPRING-BOOT-ADVANCED-GUIDE.md` section 22 (rate limiting mention)
- [`User.java`](Springboot/security-module/src/main/java/com/learning/security/entity/User.java) — `UserDetails` implementation, locked/enabled flags

---

### Sub-Task 9 — Security Headers and Content Security Policy (Junior + Senior)

**Status:** [ ] pending

**Intent:**
HTTP security headers are a quick win that is often missed by junior developers and specifically asked about in senior interviews (especially for APIs serving browser clients).

**Expected Outcomes:**
- Section written: "HTTP Security Headers"
- Covers: Spring Security's `headers()` DSL, X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, Referrer-Policy, Content-Security-Policy, Permissions-Policy
- Junior angle: that security headers exist and how to enable them in Spring Security
- Senior angle: CSP directives, nonce-based inline script allowlisting, report-only mode, HSTS preloading

**Todo List:**
1. Write Spring Security default headers — what `headers().defaultsDisabled()` implies vs defaults
2. Write each security header — X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, HSTS
3. Write Content-Security-Policy — directive syntax, `default-src 'self'`, report-only mode
4. Write Referrer-Policy and Permissions-Policy — privacy-aligned headers
5. Write configuration example — Spring Security `headers()` DSL with each header set
6. Add interview Q&A block

**Relevant Context:**
- [`SecurityConfig.java`](Springboot/security-module/src/main/java/com/learning/security/config/SecurityConfig.java) — existing config without explicit headers block (gap to fill)

---

### Sub-Task 10 — Audit Logging and Security Event Monitoring (Senior)

**Status:** [ ] pending

**Intent:**
Detecting and responding to security events is a senior-level topic that is almost never covered in beginner guides but heavily weighted in real system design.

**Expected Outcomes:**
- Section written: "Audit Logging and Security Event Monitoring"
- Covers: Spring Security's `ApplicationEventPublisher` for auth events, `AuthenticationSuccessEvent`, `AuthenticationFailureBadCredentialsEvent`, `AbstractAuthorizationEvent`, custom `AuditApplicationEvent`, structured audit log format, Spring Data Envers for entity change tracking
- Junior angle: how to listen for login success/failure events
- Senior angle: structured security audit trail, SIEM integration, immutable audit log design, Envers for data change history

**Todo List:**
1. Write Spring Security auth events — event types and how to listen with `@EventListener`
2. Write custom audit event publisher — publishing domain security events (password changed, role changed, etc.)
3. Write structured audit log format — who, what, when, from-where, outcome
4. Write Spring Data Envers — entity-level history tracking for compliance
5. Write SIEM integration angle — shipping logs to ELK / Splunk / CloudWatch
6. Add interview Q&A block

**Relevant Context:**
- `SPRING-BOOT-ADVANCED-GUIDE.md` sections 15–16 (event-driven patterns, transactional events)
- [`AuthServiceImpl.java`](Springboot/security-module/src/main/java/com/learning/security/service/impl/AuthServiceImpl.java) — existing log statements (not structured audit events)

---

### Sub-Task 11 — Reactive Security with Spring WebFlux (Senior)

**Status:** [ ] pending

**Intent:**
The project has a WebFlux migration guide (`SECURITY-WEBFLUX-IMPLEMENTATION-GUIDE.md`) but it is step-by-step code. This section provides the conceptual + comparison layer that enables developers to explain the differences confidently in interviews.

**Expected Outcomes:**
- Section written: "Reactive Security with Spring WebFlux"
- Covers: `ReactiveSecurityContextHolder` vs `SecurityContextHolder`, `WebFilter` vs `OncePerRequestFilter`, `ReactiveAuthenticationManager`, `ServerHttpSecurity` vs `HttpSecurity`, R2DBC + reactive `UserDetailsService`
- Junior angle: why the same security class names don't work in WebFlux
- Senior angle: context propagation across reactor operators, non-blocking JWT filter, `Mono<SecurityContext>` vs `ThreadLocal`

**Todo List:**
1. Write why thread-local fails in reactive — Project Reactor's execution model, operator fusion
2. Write `ReactiveSecurityContextHolder` — `withAuthentication()`, `getContext()` from Reactor context
3. Write reactive JWT filter — `WebFilter` with `Mono<Void>` return
4. Write `ServerHttpSecurity` configuration — reactive equivalent of `HttpSecurity`
5. Write reactive `UserDetailsService` — `ReactiveUserDetailsService` interface
6. Reference `SECURITY-WEBFLUX-IMPLEMENTATION-GUIDE.md` for full implementation steps
7. Add interview Q&A block

**Relevant Context:**
- [`SECURITY-WEBFLUX-IMPLEMENTATION-GUIDE.md`](./SECURITY-WEBFLUX-IMPLEMENTATION-GUIDE.md) — existing step-by-step guide
- [`JwtAuthenticationFilter.java`](Springboot/security-module/src/main/java/com/learning/security/filter/JwtAuthenticationFilter.java:28) — comment noting `WebFilter` should replace `OncePerRequestFilter` for WebFlux

---

### Sub-Task 12 — Testing Spring Security (Junior + Senior)

**Status:** [ ] pending

**Intent:**
Security tests are the most important tests in the application but the most commonly skipped by developers who are still learning. This section provides the full testing toolkit.

**Expected Outcomes:**
- Section written: "Testing Spring Security"
- Covers: `@WithMockUser`, `@WithUserDetails`, `@WithSecurityContext`, `MockMvc` with `SecurityMockMvcConfigurers.springSecurity()`, testing 401/403 responses, testing `@PreAuthorize` in service layer, `@WebMvcTest` vs `@SpringBootTest` for security tests, JWT token helper for integration tests
- Junior angle: how to write a test that passes as a specific role
- Senior angle: testing method-level security in isolation, building a JWT token factory for integration tests, testing token expiry and refresh flows

**Todo List:**
1. Write `@WithMockUser` — simplest way to set the security context in a test
2. Write `@WithUserDetails` — loads real `UserDetails` from `UserDetailsService`
3. Write `@WithSecurityContext` — fully custom security context for complex claims
4. Write `MockMvc` security tests — test 401 unauthenticated, 403 wrong role, 200 correct role
5. Write service-layer method security tests — `@PreAuthorize` in isolation
6. Write JWT integration test helper — generating a real JWT for `@SpringBootTest` tests
7. Write token expiry test pattern — clock manipulation or short TTL
8. Add interview Q&A block

**Relevant Context:**
- `Springboot/security-module/src/test/` — currently empty, tests to be added
- [`AuthController.java`](Springboot/security-module/src/main/java/com/learning/security/controller/AuthController.java) — endpoints to test

---

### Sub-Task 13 — Security Checklist and Interview Summary (All Levels)

**Status:** [ ] pending

**Intent:**
Provide a final, scannable production security checklist and a consolidated set of interview Q&A questions spanning all levels — so the guide can be used as a pre-interview revision sheet.

**Expected Outcomes:**
- Section written: "Production Security Checklist"
- Section written: "Security Interview Q&A — Junior through Senior"
- Covers 30+ Q&A items grouped by: fundamentals, JWT, authorization, OAuth2, transport, rate limiting, headers, testing, WebFlux

**Todo List:**
1. Write production checklist — authentication, authorization, transport, headers, secrets, rate limiting, logging, testing
2. Write junior Q&A — 10 questions (filter chain, BCrypt, JWT structure, CORS, session policy)
3. Write mid-level Q&A — 10 questions (refresh tokens, method security, CSRF, PKCE, account lockout)
4. Write senior Q&A — 10+ questions (OAuth2 flows, reactive security context, permission evaluator, revocation patterns, audit trails, Vault, CSP)

**Relevant Context:**
- `SPRING-BOOT-ADVANCED-GUIDE.md` section 26 (existing interview questions to avoid duplicating)
- `INTERVIEW-PREPARATION-GUIDE.md` Q7, Q8 (existing security Q&A to build on)
- All sections in this plan

---

## Output File

All sub-tasks write into a single file: **`SPRING-BOOT-SECURITY-GUIDE.md`**

The file will be structured as:
```
# Spring Boot Security Guide
## Complete Reference for Junior and Senior Developers

1. Spring Security Architecture
2. Password Security
3. JWT Authentication Deep Dive
4. Authorization — RBAC, PBAC, Method Security
5. CORS and CSRF
6. OAuth2 and OpenID Connect
7. HTTPS, Secrets Management, and Transport Security
8. Rate Limiting, Brute Force Protection, and Account Lockout
9. HTTP Security Headers
10. Audit Logging and Security Event Monitoring
11. Reactive Security with Spring WebFlux
12. Testing Spring Security
13. Production Security Checklist and Interview Q&A
```

Each section includes: concept → code pattern → this project's implementation → trade-offs → interview questions.
