# Interview Preparation Guide
## Senior Full-Stack Developer Preparation for Spring Boot + Angular Roles

This guide is designed to help you prepare for senior full-stack interviews with a practical focus on **Spring Boot**, **Angular**, architecture, debugging, coding rounds, and behavioral communication.

The strongest interview preparation combines:
- technical depth
- implementation experience
- system design clarity
- debugging discipline
- communication quality
- ownership stories

This guide is structured so you can prepare using the same project you build during the learning plan.

---

# 1. Interview Preparation Strategy

## What senior interviews usually evaluate
1. backend depth
2. frontend depth
3. system design
4. coding/problem solving
5. debugging and optimization
6. communication and ownership
7. trade-off thinking

## Your preparation model
For each topic:
- understand the concept
- implement it in your project
- explain it aloud
- answer follow-up questions
- discuss trade-offs
- connect it to production scenarios

---

# 2. How to Talk About Your Project

Use your job portal project as your anchor story.

## 2.1 Project summary template
> I built a job portal application using Spring Boot for the backend and Angular for the frontend. The backend exposes REST APIs for job management, authentication, applications, and reporting. The frontend consumes these APIs using typed services, guards, and interceptors. I focused on clean architecture, DTO-based APIs, validation, JWT security, testing, and Docker-based deployment.

## 2.2 What interviewers want to hear
- why you chose the architecture
- how you handled security
- how you modeled the database
- how frontend and backend integrate
- how you tested the system
- what trade-offs you made
- what you would improve next

## 2.3 Strong project explanation structure
1. problem/domain
2. architecture
3. key technical decisions
4. challenges faced
5. trade-offs
6. testing and deployment
7. future improvements

---

# 3. Spring Boot Interview Questions

## Q1. What is the difference between `@Component`, `@Service`, and `@Repository`?
**Answer:**
All are Spring stereotypes and register beans in the application context. `@Service` communicates business logic intent, `@Repository` communicates persistence intent and enables exception translation, and `@Component` is generic. Functionally they are similar for bean registration, but semantically they improve structure and readability.

## Q2. Why use DTOs instead of returning entities directly?
**Answer:**
DTOs decouple API contracts from persistence models. They prevent accidental exposure of internal fields, avoid lazy-loading serialization issues, support validation, and make API evolution safer.

## Q3. What is the difference between `@Transactional` on read and write methods?
**Answer:**
Write methods usually use default transactional behavior for consistency and rollback. Read methods often use `@Transactional(readOnly = true)` to communicate intent and allow optimizations. It also helps define clear transaction boundaries.

## Q4. What is the N+1 query problem?
**Answer:**
It happens when one query loads a parent list and then additional queries are triggered for each child association, causing many unnecessary DB round trips. It is commonly caused by lazy-loaded relationships accessed in loops. Solutions include fetch joins, entity graphs, projections, and better query design.

## Q5. Why disable Open Session in View?
**Answer:**
Disabling it prevents lazy-loading from happening during response serialization outside the service layer. This forces explicit transaction and fetch planning, which leads to more predictable performance and cleaner architecture.

## Q6. How do you handle exceptions globally in Spring Boot?
**Answer:**
Using `@RestControllerAdvice` with `@ExceptionHandler` methods. This centralizes error mapping, keeps controllers clean, and ensures consistent error responses.

## Q7. How does Spring Security work at a high level?
**Answer:**
Requests pass through a filter chain. Authentication filters validate credentials or tokens, populate the security context, and authorization rules determine whether the request can proceed. Method-level security can add another protection layer.

## Q8. What are the trade-offs of JWT authentication?
**Answer:**
JWT supports stateless scaling and works well for distributed systems, but revocation is harder than session-based auth. You need short-lived tokens, refresh tokens, or blacklist strategies. Token size and claim design also matter.

## Q9. When would you use caching?
**Answer:**
For frequently read, expensive, or stable data where reduced latency and DB load matter. But caching requires invalidation strategy, TTL decisions, and awareness of stale data trade-offs.

## Q10. What is the difference between optimistic and pessimistic locking?
**Answer:**
Optimistic locking assumes conflicts are rare and detects them using version checks. Pessimistic locking prevents concurrent modification by locking rows early. Optimistic locking is usually better for scalable web apps unless contention is high.

---

# 4. Angular Interview Questions

## Q1. What is the difference between template-driven and reactive forms?
**Answer:**
Template-driven forms are simpler and more declarative in templates, suitable for smaller forms. Reactive forms are model-driven, more explicit, easier to test, and better for complex validation and dynamic forms.

## Q2. What does `OnPush` change detection do?
**Answer:**
It reduces unnecessary change detection by checking a component primarily when input references change, events occur, or observable emissions are consumed. It encourages immutable patterns and improves performance.

## Q3. When would you use `switchMap`?
**Answer:**
When a new emission should cancel the previous inner observable. A common example is search input where only the latest request matters.

## Q4. What is the role of an interceptor?
**Answer:**
Interceptors handle cross-cutting HTTP concerns such as attaching auth headers, logging, retry logic, and centralized error handling.

## Q5. How do route guards differ from backend authorization?
**Answer:**
Route guards improve frontend navigation control and UX, but they are not a security boundary. Real authorization must be enforced on the backend.

## Q6. How do you prevent memory leaks in Angular?
**Answer:**
Use async pipe where possible, avoid unnecessary manual subscriptions, use cleanup helpers like `takeUntilDestroyed`, and avoid nested subscriptions.

## Q7. How would you structure a large Angular app?
**Answer:**
Use feature-based organization with core, shared, and feature areas. Keep API services centralized, separate smart and presentational components, and introduce a state/facade layer as complexity grows.

## Q8. When do you introduce state management libraries?
**Answer:**
When state becomes shared, complex, and difficult to reason about with local component state or simple services. The decision depends on complexity, team familiarity, and need for predictability.

## Q9. How do you handle backend validation errors in forms?
**Answer:**
Map backend field errors into form controls so the UI can display them consistently. This keeps frontend and backend validation aligned without duplicating all rules.

## Q10. What are common Angular performance optimizations?
**Answer:**
Lazy loading, `OnPush`, `trackBy`, avoiding heavy template expressions, reducing unnecessary subscriptions, and splitting large components.

---

# 5. Full-Stack Integration Questions

## Q1. How do you keep frontend and backend contracts aligned?
**Answer:**
Use typed DTOs/interfaces, document APIs, keep error shapes consistent, and coordinate changes carefully. OpenAPI can help generate or validate contracts.

## Q2. How do you handle authentication end-to-end?
**Answer:**
Backend authenticates credentials and issues tokens. Frontend stores and attaches tokens via interceptor. Guards protect routes, backend enforces authorization, and logout/refresh flows manage token lifecycle.

## Q3. How do you debug a 401 issue?
**Answer:**
Check whether the token exists, whether the interceptor attaches it, whether the token is expired, whether backend signature validation passes, and whether authorization rules match the user role.

## Q4. How do you handle pagination across frontend and backend?
**Answer:**
Backend exposes page, size, sort, and filter parameters with a stable paginated response shape. Frontend maps UI controls to those parameters and renders metadata consistently.

---

# 6. System Design Preparation

Senior full-stack interviews often include medium-scale system design.

## 6.1 How to answer system design questions
Use this structure:
1. clarify requirements
2. identify functional and non-functional needs
3. define core entities
4. propose high-level architecture
5. design APIs/data flow
6. discuss scaling, security, and failure handling
7. mention trade-offs

## 6.2 Example prompt
**Design a job portal platform**

### Functional requirements
- users can register/login
- employers can post jobs
- candidates can search and apply
- admins can moderate content
- notifications are sent for application updates

### Non-functional requirements
- secure authentication
- responsive search
- scalable read traffic
- reliable application submission
- auditability

### High-level architecture
- Angular frontend
- Spring Boot backend
- PostgreSQL primary database
- Redis for caching and token/session support
- object storage for resumes
- email/notification service
- optional message broker for async workflows

### Core services
- auth service
- job service
- application service
- notification service
- admin/reporting service

### Data model
- users
- roles
- companies
- jobs
- applications
- resumes
- notifications

### Scaling discussion
- cache popular job searches
- paginate all list endpoints
- index search columns
- async notifications
- separate read-heavy concerns if needed later

### Failure handling
- retries for email sending
- idempotent application submission where needed
- transactional persistence before side effects
- monitoring and alerting

---

# 7. System Design Follow-Up Questions

Be ready for:
- How would you prevent duplicate applications?
- How would you support millions of job searches?
- How would you design notifications?
- How would you store resumes?
- How would you audit admin actions?
- How would you handle eventual consistency?
- How would you version APIs?
- How would you secure internal services?

---

# 8. Coding Round Preparation

Coding rounds for senior roles often test:
- clarity
- correctness
- edge cases
- communication
- optimization reasoning

## 8.1 Coding round approach
1. restate the problem
2. ask clarifying questions
3. discuss brute-force approach
4. propose optimized approach
5. code clearly
6. test with examples
7. discuss complexity

## 8.2 Common topics
- arrays and strings
- hash maps
- sliding window
- two pointers
- stacks/queues
- trees/graphs
- recursion/backtracking
- intervals
- sorting/searching
- concurrency basics (sometimes)

---

# 9. Practice Coding Questions

## Question 1: Two Sum
Given an array and target, return indices of two numbers that add up to target.

### Java solution

```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>();

    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (seen.containsKey(complement)) {
            return new int[]{seen.get(complement), i};
        }
        seen.put(nums[i], i);
    }

    return new int[0];
}
```

### Discussion
- time: O(n)
- space: O(n)

## Question 2: Valid Parentheses

```java
public boolean isValid(String s) {
    Map<Character, Character> pairs = Map.of(
        ')', '(',
        '}', '{',
        ']', '['
    );

    Deque<Character> stack = new ArrayDeque<>();

    for (char ch : s.toCharArray()) {
        if (pairs.containsValue(ch)) {
            stack.push(ch);
        } else if (pairs.containsKey(ch)) {
            if (stack.isEmpty() || stack.pop() != pairs.get(ch)) {
                return false;
            }
        }
    }

    return stack.isEmpty();
}
```

## Question 3: Merge Intervals

```java
public int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, Comparator.comparingInt(a -> a[0]));
    List<int[]> merged = new ArrayList<>();

    for (int[] current : intervals) {
        if (merged.isEmpty() || merged.get(merged.size() - 1)[1] < current[0]) {
            merged.add(current);
        } else {
            merged.get(merged.size() - 1)[1] =
                Math.max(merged.get(merged.size() - 1)[1], current[1]);
        }
    }

    return merged.toArray(new int[0][]);
}
```

---

# 10. Backend Debugging Scenarios

## Scenario 1: API is slow in production
### How to answer
- identify whether slowness is DB, network, CPU, or external dependency related
- inspect logs and metrics
- check slow queries
- inspect thread pools and connection pools
- review recent deployments
- reproduce with profiling if possible

## Scenario 2: Random 500 errors on job creation
### Investigation path
- inspect logs with correlation IDs
- check validation edge cases
- inspect DB constraints
- verify null handling
- review recent schema changes
- add targeted tests

## Scenario 3: Duplicate notifications sent
### Investigation path
- check retry logic
- inspect scheduler overlap
- verify event listener idempotency
- inspect transaction boundaries
- confirm whether event published multiple times

---

# 11. Frontend Debugging Scenarios

## Scenario 1: UI shows stale job list
### Investigation path
- inspect caching/state layer
- verify API response freshness
- check whether observable stream is replaying stale data
- inspect route reuse or component lifecycle
- verify mutation vs immutable update issues

## Scenario 2: Memory usage grows after navigation
### Investigation path
- inspect manual subscriptions
- check event listeners
- verify component destruction
- use browser performance tools
- review long-lived subjects

## Scenario 3: Login works but protected API fails
### Investigation path
- inspect token storage
- inspect interceptor attachment
- inspect backend CORS/security config
- verify token expiration
- inspect role mismatch

---

# 12. Behavioral Interview Preparation

Senior interviews strongly evaluate ownership and collaboration.

## Common behavioral themes
- handling ambiguity
- resolving production incidents
- mentoring others
- improving code quality
- dealing with disagreement
- prioritization under pressure
- learning from failure

## STAR format
Use:
- Situation
- Task
- Action
- Result

## Example question
**Tell me about a time you improved a system.**

### Strong answer structure
- describe the problem clearly
- explain why it mattered
- explain your analysis
- explain the changes you drove
- quantify the result if possible
- mention lessons learned

---

# 13. Behavioral Questions to Practice

1. Tell me about a difficult bug you solved.
2. Describe a time you improved performance.
3. Tell me about a disagreement on architecture.
4. Describe a production incident you handled.
5. Tell me about a time you mentored someone.
6. Describe a time requirements were unclear.
7. Tell me about a trade-off you made under time pressure.
8. Describe a mistake you made and what you learned.

---

# 14. Resume and Project Presentation Tips

## What to emphasize
- impact
- ownership
- architecture decisions
- measurable improvements
- testing and deployment maturity
- cross-functional collaboration

## Weak phrasing
- “Worked on APIs”
- “Used Angular”
- “Handled bugs”

## Strong phrasing
- “Designed and implemented DTO-based REST APIs for job and application workflows using Spring Boot and PostgreSQL”
- “Built Angular feature modules with guards, interceptors, and reactive forms for secure employer and candidate workflows”
- “Improved API response times by introducing caching and query optimization”
- “Containerized the application using Docker and documented deployment and production readiness steps”

---

# 15. Mock Interview Practice Plan

## Daily
- 1 coding problem
- 15 minutes of concept revision
- 10 minutes explaining one project decision aloud

## 3 times per week
- 1 backend deep-dive session
- 1 frontend deep-dive session
- 1 behavioral practice session

## Weekly
- 1 system design mock
- 1 full project walkthrough
- 1 debugging scenario discussion

---

# 16. Rapid-Fire Question Bank

## Spring Boot
- What is bean scope?
- What is dependency injection?
- What is the difference between `@Bean` and `@Component`?
- What is the difference between `save()` and `saveAndFlush()`?
- What is lazy vs eager loading?
- What is optimistic locking?
- What is a transaction propagation mode?

## Angular
- What is a standalone component?
- What is the difference between `Subject` and `BehaviorSubject`?
- What is the async pipe doing?
- What is the difference between `ngOnInit` and constructor?
- What is `trackBy`?
- What is the difference between `setValue` and `patchValue`?

## Full-stack
- How do you handle CORS?
- How do you secure file uploads?
- How do you trace a request across services?
- How do you handle backward compatibility?

---

# 17. Self-Evaluation Checklist

Before interviews, verify that you can confidently explain:

- your project architecture
- authentication flow
- DTO and validation strategy
- transaction boundaries
- caching decisions
- Angular app structure
- RxJS operator choices
- state management approach
- testing strategy
- deployment approach
- one production incident story
- one mentoring/collaboration story

---

# 18. Final Week Preparation Plan

## Day 1
- backend revision
- Spring Boot Q&A
- one coding problem

## Day 2
- Angular revision
- RxJS and forms review
- one coding problem

## Day 3
- system design practice
- architecture explanation
- behavioral answers

## Day 4
- debugging scenarios
- security review
- one coding problem

## Day 5
- mock interview
- resume/project walkthrough
- weak area revision

## Day 6
- rapid-fire revision
- light coding
- rest and confidence building

## Day 7
- final review only
- no heavy cramming

---

# 19. Final Advice

Senior interviews are rarely passed by memorization alone.

You need to show that you can:
- build real systems
- reason about trade-offs
- debug under uncertainty
- communicate clearly
- take ownership
- learn continuously

Use your project as proof. Every feature you implement should become a story you can explain with confidence.