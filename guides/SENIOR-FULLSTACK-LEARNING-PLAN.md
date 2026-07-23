# Senior Full-Stack Learning Plan
## Spring Boot + Angular Foundations to Architecture Thinking

This guide covers the core learning path for becoming strong in full-stack engineering with **Spring Boot** and **Angular**. It focuses on the phases that build your backend foundation, API maturity, architecture habits, and engineering discipline.

The goal is not only to help you build features, but to help you think like a senior engineer:
- structure code for change
- design APIs intentionally
- model data carefully
- test critical behavior
- reason about trade-offs
- communicate decisions clearly

---

# 1. Learning Objectives

By the end of this guide, you should be able to:

- create a clean Spring Boot application structure
- design layered backend architecture
- build REST APIs with DTOs and validation
- model relational data in PostgreSQL
- manage schema changes with migrations
- implement pagination, filtering, and sorting
- write unit and integration tests
- connect Angular to backend APIs cleanly
- explain why one design is better than another in a given context

---

# 2. Recommended Project: Job Portal Domain

Use a realistic domain so your learning maps to interview discussions and production-style thinking.

## Core entities
- User
- Role
- Job
- Company
- Application
- Resume
- Skill
- Notification

## Example user flows
- Candidate registers and logs in
- Employer creates and manages jobs
- Candidate searches and applies for jobs
- Admin reviews platform activity
- Notifications are sent for application updates

This domain gives you:
- CRUD operations
- relationships
- authorization rules
- search/filtering
- async workflows
- reporting opportunities

---

# 3. Suggested Backend Architecture

Use a layered architecture first. Learn where responsibilities belong before exploring more advanced patterns.

## Standard layers
- **Controller**: handles HTTP requests/responses
- **Service**: business logic and orchestration
- **Repository**: database access
- **Entity**: persistence model
- **DTO**: API contract model
- **Mapper**: conversion between entity and DTO

## Example package structure

```text
com.example.jobportal
├── config
├── controller
├── dto
├── entity
├── exception
├── mapper
├── repository
├── service
│   ├── impl
├── specification
├── security
└── util
```

## Why this matters
A senior engineer separates concerns so that:
- controllers stay thin
- business logic is testable
- persistence details do not leak everywhere
- API contracts can evolve safely

---

# 4. Phase 1: Spring Boot Foundation

## 4.1 Create the project

Recommended dependencies:
- Spring Web
- Spring Data JPA
- Validation
- PostgreSQL Driver
- Lombok
- Spring Security
- Flyway
- Spring Boot Actuator
- Testcontainers
- Spring Boot Test

## 4.2 Basic configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/job_portal
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
    open-in-view: false
  flyway:
    enabled: true

server:
  port: 8080
```

## Why `open-in-view: false`
This forces you to think carefully about transaction boundaries and lazy loading instead of relying on accidental session availability during serialization.

---

# 5. Phase 1 Implementation: First CRUD Feature

Start with **Job**.

## 5.1 Entity example

```java
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(nullable = false)
    private BigDecimal salaryMin;

    @Column(nullable = false)
    private BigDecimal salaryMax;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

## 5.2 DTOs

```java
public record JobRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String location,
    @NotNull JobStatus status,
    @NotNull @Positive BigDecimal salaryMin,
    @NotNull @Positive BigDecimal salaryMax
) {}
```

```java
public record JobResponse(
    Long id,
    String title,
    String description,
    String location,
    JobStatus status,
    BigDecimal salaryMin,
    BigDecimal salaryMax,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
```

## Why DTOs instead of exposing entities
- prevents accidental field exposure
- decouples API from persistence model
- supports validation cleanly
- makes versioning easier later

## 5.3 Repository

```java
public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findByStatus(JobStatus status, Pageable pageable);
}
```

## 5.4 Service

```java
@Service
@RequiredArgsConstructor
@Transactional
public class JobService {

    private final JobRepository jobRepository;

    public JobResponse create(JobRequest request) {
        Job job = new Job();
        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setLocation(request.location());
        job.setStatus(request.status());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());

        Job saved = jobRepository.save(job);
        return map(saved);
    }

    @Transactional(readOnly = true)
    public JobResponse getById(Long id) {
        Job job = jobRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        return map(job);
    }

    private JobResponse map(Job job) {
        return new JobResponse(
            job.getId(),
            job.getTitle(),
            job.getDescription(),
            job.getLocation(),
            job.getStatus(),
            job.getSalaryMin(),
            job.getSalaryMax(),
            job.getCreatedAt(),
            job.getUpdatedAt()
        );
    }
}
```

## 5.5 Controller

```java
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> create(@Valid @RequestBody JobRequest request) {
        JobResponse response = jobService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getById(id));
    }
}
```

---

# 6. Validation and Error Handling

A senior backend is predictable when things go wrong.

## 6.1 Global exception handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "timestamp", Instant.now(),
            "status", 404,
            "error", "Not Found",
            "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                DefaultMessageSourceResolvable::getDefaultMessage,
                (a, b) -> a
            ));

        return ResponseEntity.badRequest().body(Map.of(
            "timestamp", Instant.now(),
            "status", 400,
            "error", "Validation Failed",
            "fields", fieldErrors
        ));
    }
}
```

## Design principle
Clients should receive:
- stable error shape
- meaningful messages
- correct HTTP status codes
- enough detail to fix bad requests
- no internal implementation leakage

---

# 7. Database Design Fundamentals

## 7.1 Think in relationships
Examples:
- one company has many jobs
- one candidate has many applications
- one job has many applications
- one user has one or many roles depending on design

## 7.2 Normalize first, optimize later
Start with a clean relational model. Denormalize only when you have a measured performance reason.

## 7.3 Add constraints
Use:
- `NOT NULL`
- unique constraints
- foreign keys
- indexes on search/filter columns

## Example migration

```sql
CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    website VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE jobs (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    salary_min NUMERIC(12,2) NOT NULL,
    salary_max NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_location ON jobs(location);
```

---

# 8. Flyway Migration Strategy

Never rely on Hibernate auto-DDL in serious environments.

## Why Flyway
- schema changes are versioned
- team changes are reproducible
- production deployments are safer
- rollback planning becomes clearer

## Naming convention
```text
V1__create_companies_and_jobs.sql
V2__add_users_and_roles.sql
V3__create_applications_table.sql
```

## Good migration habits
- keep migrations small
- never edit an already-applied migration in shared environments
- prefer additive changes
- test migrations on realistic data

---

# 9. Pagination, Sorting, and Filtering

These are expected in real APIs and frequently discussed in interviews.

## Controller example

```java
@GetMapping
public ResponseEntity<Page<JobResponse>> search(
    @RequestParam(required = false) JobStatus status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "createdAt") String sortBy,
    @RequestParam(defaultValue = "desc") String direction
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
    return ResponseEntity.ok(jobService.search(status, pageable));
}
```

## Service example

```java
@Transactional(readOnly = true)
public Page<JobResponse> search(JobStatus status, Pageable pageable) {
    Page<Job> jobs = status == null
        ? jobRepository.findAll(pageable)
        : jobRepository.findByStatus(status, pageable);

    return jobs.map(this::map);
}
```

## Senior considerations
- validate page size upper bounds
- avoid exposing arbitrary sort fields without control
- document defaults
- think about index support for common queries

---

# 10. Specifications and Dynamic Queries

As filtering grows, repository method names become unmanageable.

## Example specification

```java
public class JobSpecification {

    public static Specification<Job> hasStatus(JobStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Job> hasLocation(String location) {
        return (root, query, cb) ->
            location == null || location.isBlank() ? null :
                cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }
}
```

## Why this matters
Specifications help when:
- filters are optional
- combinations grow
- business search logic evolves
- you want composable query logic

---

# 11. Testing Strategy

A senior engineer knows what to test, not just how.

## 11.1 Unit tests
Use for:
- service logic
- validation helpers
- mappers
- utility classes

## 11.2 Integration tests
Use for:
- repository queries
- controller + service + DB flow
- security rules
- transaction behavior

## 11.3 Test pyramid mindset
- many unit tests
- fewer integration tests
- minimal but meaningful end-to-end tests

## Example service unit test

```java
@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    @Test
    void shouldCreateJob() {
        JobRequest request = new JobRequest(
            "Java Developer",
            "Build APIs",
            "Bangalore",
            JobStatus.OPEN,
            BigDecimal.valueOf(1000000),
            BigDecimal.valueOf(1800000)
        );

        Job saved = new Job();
        saved.setId(1L);
        saved.setTitle("Java Developer");
        saved.setDescription("Build APIs");
        saved.setLocation("Bangalore");
        saved.setStatus(JobStatus.OPEN);
        saved.setSalaryMin(BigDecimal.valueOf(1000000));
        saved.setSalaryMax(BigDecimal.valueOf(1800000));

        when(jobRepository.save(any(Job.class))).thenReturn(saved);

        JobResponse response = jobService.create(request);

        assertEquals(1L, response.id());
        assertEquals("Java Developer", response.title());
        verify(jobRepository).save(any(Job.class));
    }
}
```

## Example integration test

```java
@SpringBootTest
@AutoConfigureMockMvc
class JobControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn404WhenJobNotFound() throws Exception {
        mockMvc.perform(get("/api/jobs/999"))
            .andExpect(status().isNotFound());
    }
}
```

---

# 12. Transaction Boundaries

This is a common senior interview topic.

## Rules of thumb
- put transactions at service layer
- use `readOnly = true` for read operations
- keep transactions short
- avoid remote calls inside transactions
- understand rollback behavior for checked vs unchecked exceptions

## Example
Bad:
- start DB transaction
- call external payment API
- wait for network
- update DB

Better:
- persist intent/state
- call external service outside critical transaction when possible
- reconcile asynchronously if needed

---

# 13. API Design Principles

## Good API design means:
- consistent naming
- predictable status codes
- stable response shapes
- explicit validation
- backward compatibility awareness

## Example endpoints
```text
POST   /api/jobs
GET    /api/jobs/{id}
GET    /api/jobs
PUT    /api/jobs/{id}
PATCH  /api/jobs/{id}/status
DELETE /api/jobs/{id}
```

## Prefer explicit contracts
Avoid returning raw entities with lazy relationships and internal fields.

## Think about idempotency
- `PUT` should be idempotent
- `POST` usually creates
- `PATCH` is partial update
- `DELETE` should be safe to repeat from client perspective if resource is already gone, depending on API policy

---

# 14. Angular Integration Mindset

Even though this guide is backend-heavy, think full-stack from the start.

## Frontend integration expectations
Your Angular app should:
- call typed APIs
- handle loading/error states
- use environment-based API URLs
- centralize HTTP concerns with interceptors
- avoid duplicating backend validation rules blindly

## Example Angular service

```typescript
@Injectable({ providedIn: 'root' })
export class JobsApiService {
  constructor(private http: HttpClient) {}

  getJob(id: number): Observable<JobResponse> {
    return this.http.get<JobResponse>(`/api/jobs/${id}`);
  }

  createJob(payload: JobRequest): Observable<JobResponse> {
    return this.http.post<JobResponse>('/api/jobs', payload);
  }
}
```

## Full-stack thinking
When backend returns validation errors, frontend should map them into user-friendly form feedback.

---

# 15. Code Review Checklist for Yourself

Before marking a feature complete, ask:

- Is the controller thin?
- Is business logic in the service?
- Are DTOs used correctly?
- Are validation rules explicit?
- Are exceptions handled consistently?
- Are queries efficient enough?
- Are tests covering critical paths?
- Is naming clear?
- Would another developer understand this quickly?
- Can this design evolve without major rewrites?

---

# 16. Common Mistakes to Avoid

## Backend mistakes
- exposing entities directly
- fat controllers
- business logic in repositories
- missing validation
- no migration strategy
- no indexes on common filters
- catching generic exceptions everywhere
- long transactions
- weak test coverage

## Full-stack mistakes
- tightly coupling frontend to unstable backend responses
- inconsistent error handling
- no loading states
- no pagination strategy
- ignoring CORS/security early

---

# 17. Senior-Level Discussion Topics

Be ready to explain:

1. Why use DTOs instead of entities?
2. Why disable open session in view?
3. How do you design pagination for large datasets?
4. When would you use Specifications vs QueryDSL vs custom queries?
5. How do you structure exception handling?
6. What belongs in controller vs service?
7. How do you test repository queries?
8. How do you evolve database schema safely?
9. How do you prevent N+1 query issues?
10. How do you design APIs for frontend stability?

---

# 18. Practice Exercises

## Exercise 1: Complete Job CRUD
Implement:
- create
- get by id
- update
- delete
- list with pagination

## Exercise 2: Add Company relationship
- one company to many jobs
- return company summary in job response
- avoid infinite serialization loops

## Exercise 3: Add filtering
Support:
- status
- location
- salary range

## Exercise 4: Add validation
- salaryMin must be <= salaryMax
- title length constraints
- description minimum length

## Exercise 5: Add tests
- service unit tests
- controller integration tests
- repository query tests

---

# 19. Milestone Definition

You have completed the foundation phase when you can demonstrate:

- a working Spring Boot backend
- PostgreSQL integration
- Flyway migrations
- DTO-based CRUD APIs
- validation and exception handling
- pagination/filtering
- unit and integration tests
- Angular consuming the APIs successfully

---

# 20. What to Study Next

After completing this guide, move to:

1. `SPRING-BOOT-ADVANCED-GUIDE.md`
   - security
   - caching
   - async processing
   - observability

2. `ANGULAR-ADVANCED-GUIDE.md`
   - state management
   - advanced RxJS
   - scalable frontend architecture

3. `INTERVIEW-PREPARATION-GUIDE.md`
   - deep-dive questions
   - system design
   - coding rounds

---

# 21. Final Advice

Do not rush into advanced topics before your fundamentals are stable.

A strong senior engineer is usually excellent at basics:
- clear boundaries
- predictable APIs
- safe schema evolution
- thoughtful testing
- maintainable code
- calm debugging

Master those first. Then the advanced topics become much easier and much more meaningful.