# Spring Security + WebFlux Implementation Guide
## Complete Migration from Spring MVC to Reactive WebFlux with JWT Authentication

---

## 📋 Overview

This guide provides step-by-step instructions to:
1. Implement Spring Security with JWT authentication
2. Migrate from Spring MVC to Spring WebFlux (Reactive)
3. Convert JPA to R2DBC for reactive database access
4. Configure reactive security with role-based access control

---

## Phase 1: Security Module Setup & Configuration

### Step 1.1: Update Security Module Dependencies

**File: `Springboot/security-module/pom.xml`**

```xml
<dependencies>
    <!-- Common Module -->
    <dependency>
        <groupId>com.learning</groupId>
        <artifactId>common-module</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Spring Security for WebFlux -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Reactive Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    
    <!-- Reactive Data R2DBC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    </dependency>
    
    <!-- PostgreSQL R2DBC Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>r2dbc-postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- JWT Dependencies -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Validation & Lombok -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### Step 1.2: Database Migration Script

**File: `Springboot/security-module/src/main/resources/db/migration/V1__Create_security_tables.sql`**

```sql
-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- User-Role join table
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE(user_id, role_id)
);

-- Refresh tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('ROLE_USER', 'Standard user role'),
    ('ROLE_RECRUITER', 'Recruiter role for posting jobs'),
    ('ROLE_ADMIN', 'Administrator role with full access')
ON CONFLICT (name) DO NOTHING;

-- Create indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
```

### Step 1.3: Security Configuration Properties

**File: `Springboot/security-module/src/main/resources/application-security.yaml`**

```yaml
security:
  jwt:
    secret-key: ${JWT_SECRET_KEY:your-256-bit-secret-key-change-in-production}
    expiration: 86400000  # 24 hours
    refresh-expiration: 604800000  # 7 days
    issuer: job-portal-service
    
  cors:
    allowed-origins:
      - http://localhost:4200
      - http://localhost:3000
    allowed-methods:
      - GET
      - POST
      - PUT
      - DELETE
      - OPTIONS
    allowed-headers: "*"
    allow-credentials: true
    max-age: 3600
```

---

## Phase 2: JWT & Authentication Implementation

### Step 2.1: JWT Configuration Properties Class

**File: `Springboot/security-module/src/main/java/com/learning/security/config/JwtProperties.java`**

```java
package com.learning.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    private String secretKey;
    private Long expiration;
    private Long refreshExpiration;
    private String issuer;
}
```

### Step 2.2: JWT Service

**File: `Springboot/security-module/src/main/java/com/learning/security/service/JwtService.java`**

```java
package com.learning.security.service;

import com.learning.security.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {
    
    private final JwtProperties jwtProperties;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return buildToken(extraClaims, userDetails, jwtProperties.getExpiration());
    }
    
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtProperties.getRefreshExpiration());
    }
    
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
```

### Step 2.3: Authentication DTOs

**Create these files in `Springboot/security-module/src/main/java/com/learning/security/dto/`:**

**LoginRequest.java:**
```java
package com.learning.security.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
}
```

**RegisterRequest.java:**
```java
package com.learning.security.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank @Email
    private String email;
    
    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
    private String password;
    
    @NotBlank
    private String firstName;
    
    @NotBlank
    private String lastName;
    
    private String phone;
}
```

**AuthResponse.java:**
```java
package com.learning.security.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo user;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private List<String> roles;
    }
}
```

---

## Phase 3: Reactive Entities & Repositories

### Step 3.1: User Entity (R2DBC)

**File: `Springboot/security-module/src/main/java/com/learning/security/entity/User.java`**

```java
package com.learning.security.entity;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {
    @Id
    private Long id;
    
    private String email;
    private String password;
    
    @Column("first_name")
    private String firstName;
    
    @Column("last_name")
    private String lastName;
    
    private String phone;
    private Boolean enabled;
    
    @Column("account_non_expired")
    private Boolean accountNonExpired;
    
    @Column("account_non_locked")
    private Boolean accountNonLocked;
    
    @Column("credentials_non_expired")
    private Boolean credentialsNonExpired;
    
    @Column("email_verified")
    private Boolean emailVerified;
    
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Column("last_login")
    private LocalDateTime lastLogin;
}
```

### Step 3.2: Reactive Repositories

**UserRepository.java:**
```java
package com.learning.security.repository;

import com.learning.security.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
}
```

**UserRoleRepository.java:**
```java
package com.learning.security.repository;

import org.springframework.data.r2dbc.repository.*;
import reactor.core.publisher.Flux;

public interface UserRoleRepository extends R2dbcRepository<UserRole, Long> {
    @Query("SELECT r.name FROM roles r INNER JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = :userId")
    Flux<String> findRoleNamesByUserId(Long userId);
}
```

---

## Phase 4: WebFlux Migration

### Step 4.1: Update Job Service Dependencies

**File: `Springboot/job-service/pom.xml`**

Remove these:
```xml
<!-- REMOVE -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

Add these:
```xml
<!-- ADD -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>r2dbc-postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Step 4.2: Update Application Configuration

**File: `Springboot/job-service/src/main/resources/application.yaml`**

```yaml
spring:
  application:
    name: job-portal-service
    
  # R2DBC Configuration (Reactive)
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/job_portal_db
    username: postgres
    password: postgres
    pool:
      initial-size: 10
      max-size: 20
      
  # Flyway still uses JDBC
  flyway:
    url: jdbc:postgresql://localhost:5432/job_portal_db
    user: postgres
    password: postgres
    baseline-on-migrate: true
    
server:
  port: 8080
```

### Step 4.3: Convert Job Entity to R2DBC

**File: `Springboot/job-service/src/main/java/com/learning/job_portal_service/entity/Job.java`**

Replace JPA annotations with R2DBC:
```java
package com.learning.job_portal_service.entity;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("jobs")  // R2DBC annotation
public class Job {
    @Id
    private Long id;
    
    private String title;
    private String description;
    private String location;
    private JobStatus status;
    
    @Column("salary_min")
    private BigDecimal salaryMin;
    
    @Column("salary_max")
    private BigDecimal salaryMax;
    
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
```

### Step 4.4: Convert Repository to Reactive

**File: `Springboot/job-service/src/main/java/com/learning/job_portal_service/repository/JobRepository.java`**

```java
package com.learning.job_portal_service.repository;

import com.learning.job_portal_service.entity.Job;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface JobRepository extends R2dbcRepository<Job, Long> {
    Flux<Job> findByStatus(JobStatus status);
    Flux<Job> findByLocationContainingIgnoreCase(String location);
}
```

### Step 4.5: Convert Service to Reactive

**File: `Springboot/job-service/src/main/java/com/learning/job_portal_service/service/JobService.java`**

```java
package com.learning.job_portal_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.*;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    
    public Mono<JobResponse> create(JobRequest request) {
        Job job = Job.builder()
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .status(request.status())
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .build();
        
        return jobRepository.save(job)
                .map(this::toResponse);
    }
    
    public Mono<JobResponse> findById(Long id) {
        return jobRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Job not found")))
                .map(this::toResponse);
    }
    
    public Flux<JobResponse> findAll() {
        return jobRepository.findAll()
                .map(this::toResponse);
    }
    
    public Mono<Void> delete(Long id) {
        return jobRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Job not found")))
                .flatMap(jobRepository::delete);
    }
    
    private JobResponse toResponse(Job job) {
        return new JobResponse(/* map fields */);
    }
}
```

### Step 4.6: Convert Controller to Reactive

**File: `Springboot/job-service/src/main/java/com/learning/job_portal_service/controller/JobController.java`**

```java
package com.learning.job_portal_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public Mono<JobResponse> createJob(@Valid @RequestBody JobRequest request) {
        return jobService.create(request);
    }
    
    @GetMapping
    public Flux<JobResponse> getAllJobs() {
        return jobService.findAll();
    }
    
    @GetMapping("/{id}")
    public Mono<JobResponse> getJobById(@PathVariable Long id) {
        return jobService.findById(id);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> deleteJob(@PathVariable Long id) {
        return jobService.delete(id);
    }
}
```

---

## Phase 5: Reactive Security Configuration

### Step 5.1: JWT Authentication Filter (WebFlux)

**File: `Springboot/security-module/src/main/java/com/learning/security/filter/JwtAuthenticationWebFilter.java`**

```java
package com.learning.security.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {
    private final JwtService jwtService;
    private final ReactiveUserDetailsService userDetailsService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }
        
        String jwt = authHeader.substring(7);
        String username = jwtService.extractUsername(jwt);
        
        if (username != null) {
            return userDetailsService.findByUsername(username)
                    .flatMap(userDetails -> {
                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());
                            
                            return chain.filter(exchange)
                                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                        }
                        return chain.filter(exchange);
                    });
        }
        
        return chain.filter(exchange);
    }
}
```

### Step 5.2: Security Configuration (WebFlux)

**File: `Springboot/security-module/src/main/java/com/learning/security/config/SecurityConfig.java`**

```java
package com.learning.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationWebFilter jwtAuthFilter;
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(auth -> auth
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/jobs/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### Step 5.3: Authentication Controller

**File: `Springboot/security-module/src/main/java/com/learning/security/controller/AuthController.java`**

```java
package com.learning.security.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/register")
    public Mono<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
    
    @PostMapping("/login")
    public Mono<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
```

---

## Phase 6: Testing

### Step 6.1: Test Security Configuration

```bash
# Register a new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@1234",
    "firstName": "John",
    "lastName": "Doe"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test@1234"
  }'

# Access protected endpoint
curl -X GET http://localhost:8080/api/jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Phase 7: Key Interview Topics

### WebFlux vs Spring MVC
- **Blocking vs Non-blocking**: MVC blocks threads, WebFlux uses event loop
- **Scalability**: WebFlux handles more concurrent requests with fewer threads
- **Backpressure**: WebFlux supports reactive streams backpressure
- **Use Cases**: WebFlux for high concurrency, MVC for traditional CRUD

### R2DBC vs JPA
- **Reactive**: R2DBC is fully reactive, JPA is blocking
- **Connection Pooling**: R2DBC uses reactive connection pools
- **Relationships**: R2DBC doesn't support lazy loading like JPA
- **Transactions**: R2DBC uses reactive transactions

### JWT Authentication
- **Stateless**: No server-side session storage
- **Token Structure**: Header.Payload.Signature
- **Security**: Use strong secret keys, short expiration times
- **Refresh Tokens**: Separate long-lived tokens for renewal

### Security Best Practices
- **Password Encoding**: Use BCrypt with proper strength
- **CORS**: Configure properly for production
- **CSRF**: Disable for stateless JWT APIs
- **Rate Limiting**: Implement to prevent abuse
- **Input Validation**: Always validate and sanitize inputs

---

## Phase 8: Next Steps

1. **Implement Refresh Token Logic**
2. **Add Email Verification**
3. **Implement Password Reset**
4. **Add Rate Limiting**
5. **Configure Redis for Token Blacklisting**
6. **Add Comprehensive Tests**
7. **Set up Monitoring and Logging**
8. **Deploy to Production**

---

## 📚 Additional Resources

- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [R2DBC Documentation](https://r2dbc.io/)
- [JWT.io](https://jwt.io/)
- [Project Reactor](https://projectreactor.io/)

---

**Remember**: This is a reactive implementation. All operations return `Mono<T>` (single value) or `Flux<T>` (multiple values). Never block in reactive code!