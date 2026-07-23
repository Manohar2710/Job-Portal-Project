# Angular Advanced Guide
## Scalable Frontend Architecture, RxJS, State Management, Performance, and Testing

This guide focuses on the frontend engineering practices expected from a strong Angular developer working in a full-stack Spring Boot + Angular environment.

It assumes you already understand:
- components
- templates
- services
- routing
- forms
- HTTP basics

This guide moves into:
- scalable Angular architecture
- smart vs presentational components
- RxJS patterns
- state management
- route guards and interceptors
- reactive forms at scale
- performance optimization
- testing strategy
- frontend interview depth

---

# 1. Learning Objectives

By the end of this guide, you should be able to:

- structure Angular applications for growth
- separate UI concerns from orchestration logic
- use RxJS intentionally instead of mechanically
- manage state predictably
- secure frontend routes and API calls
- build reusable form patterns
- optimize rendering and bundle performance
- write maintainable unit tests
- explain Angular architecture decisions in interviews

---

# 2. Recommended Frontend Architecture

As the application grows, avoid putting everything into a flat component/service structure.

## Suggested structure

```text
src/app
├── core
│   ├── guards
│   ├── interceptors
│   ├── services
│   └── models
├── shared
│   ├── components
│   ├── pipes
│   ├── directives
│   └── utils
├── features
│   ├── jobs
│   ├── auth
│   ├── applications
│   └── admin
└── app.routes.ts
```

## Layering idea
- **core**: singleton services, auth, interceptors, app-wide concerns
- **shared**: reusable UI building blocks
- **features**: domain-specific screens and logic
- **models**: typed contracts for API communication

## Why this matters
A senior frontend engineer optimizes for:
- discoverability
- reuse
- testability
- low coupling
- easier onboarding for other developers

---

# 3. Smart vs Presentational Components

This is one of the most useful frontend design habits.

## Smart components
Responsible for:
- fetching data
- coordinating services
- handling route params
- managing state transitions

## Presentational components
Responsible for:
- rendering inputs
- emitting outputs
- staying reusable and UI-focused

## Example

### Smart container

```typescript
@Component({
  selector: 'app-job-list-page',
  template: `
    <app-job-filters
      [filters]="filters"
      (filtersChange)="onFiltersChange($event)">
    </app-job-filters>

    <app-job-list
      [jobs]="jobs$ | async"
      [loading]="loading$ | async"
      (selectJob)="openJob($event)">
    </app-job-list>
  `
})
export class JobListPageComponent {
  jobs$ = this.jobsFacade.jobs$;
  loading$ = this.jobsFacade.loading$;
  filters = { status: 'OPEN', location: '' };

  constructor(private jobsFacade: JobsFacade) {}

  onFiltersChange(filters: JobFilters): void {
    this.jobsFacade.loadJobs(filters);
  }

  openJob(jobId: number): void {
    // navigate
  }
}
```

### Presentational component

```typescript
@Component({
  selector: 'app-job-list',
  template: `
    <ng-container *ngIf="!loading; else loadingTpl">
      <div *ngFor="let job of jobs; trackBy: trackById" (click)="selectJob.emit(job.id)">
        {{ job.title }} - {{ job.location }}
      </div>
    </ng-container>

    <ng-template #loadingTpl>Loading...</ng-template>
  `
})
export class JobListComponent {
  @Input() jobs: JobResponse[] = [];
  @Input() loading = false;
  @Output() selectJob = new EventEmitter<number>();

  trackById(_: number, job: JobResponse): number {
    return job.id;
  }
}
```

## Benefits
- easier testing
- reusable UI
- cleaner orchestration
- less duplicated logic

---

# 4. RxJS Mental Models

Do not memorize operators blindly. Understand stream behavior.

## Key concepts
- observable: stream of values over time
- subscription: execution of the stream
- operator: transforms stream behavior
- subject: multicast source
- hot vs cold observables
- cancellation and cleanup

## Common operators and when to use them

### `map`
Transform values.

### `switchMap`
Use when a new request should cancel the previous one.
Example: search input.

### `mergeMap`
Use when multiple inner operations can run concurrently.

### `concatMap`
Use when order matters and operations should run sequentially.

### `exhaustMap`
Use when repeated triggers should be ignored until current work finishes.
Example: prevent duplicate form submissions.

### `debounceTime`
Useful for search inputs.

### `distinctUntilChanged`
Avoid duplicate emissions.

### `catchError`
Handle errors without breaking the entire UI flow.

### `shareReplay`
Cache/reuse stream results carefully.

---

# 5. Search Example with RxJS

```typescript
searchControl = new FormControl('');

jobs$ = this.searchControl.valueChanges.pipe(
  debounceTime(300),
  distinctUntilChanged(),
  switchMap(term =>
    this.jobsApi.searchJobs(term ?? '').pipe(
      catchError(() => of([]))
    )
  )
);
```

## Why `switchMap`
If the user types quickly, older requests are canceled and only the latest result matters.

---

# 6. HTTP Layer Design

Avoid scattering raw HTTP calls across many components.

## Recommended pattern
- API service for backend communication
- facade/store service for orchestration
- components consume observables

## Example API service

```typescript
@Injectable({ providedIn: 'root' })
export class JobsApiService {
  constructor(private http: HttpClient) {}

  getJobs(params: JobSearchParams): Observable<PageResponse<JobResponse>> {
    return this.http.get<PageResponse<JobResponse>>('/api/jobs', { params: buildHttpParams(params) });
  }

  getJob(id: number): Observable<JobResponse> {
    return this.http.get<JobResponse>(`/api/jobs/${id}`);
  }

  createJob(payload: JobRequest): Observable<JobResponse> {
    return this.http.post<JobResponse>('/api/jobs', payload);
  }
}
```

## Why this matters
- centralizes API contracts
- simplifies mocking in tests
- reduces duplication
- makes refactoring easier

---

# 7. Interceptors

Interceptors are ideal for cross-cutting HTTP concerns.

## Common use cases
- attach JWT token
- handle 401 responses
- log requests
- normalize errors
- add correlation headers

## Auth interceptor example

```typescript
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authStorage: AuthStorageService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authStorage.getAccessToken();

    if (!token) {
      return next.handle(req);
    }

    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    return next.handle(cloned);
  }
}
```

## Error interceptor example

```typescript
@Injectable()
export class ApiErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        // map backend error shape to UI-friendly format
        return throwError(() => error);
      })
    );
  }
}
```

---

# 8. Route Guards

Guards protect navigation based on auth or business rules.

## Common guards
- auth guard
- role guard
- unsaved changes guard

## Example auth guard

```typescript
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};
```

## Example role guard

```typescript
export const employerGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.hasRole('EMPLOYER')
    ? true
    : router.createUrlTree(['/forbidden']);
};
```

## Important note
Frontend guards improve UX, but backend authorization remains the real security boundary.

---

# 9. Reactive Forms at Scale

Reactive forms are powerful when forms become dynamic and validation-heavy.

## Example form

```typescript
jobForm = this.fb.group({
  title: ['', [Validators.required, Validators.maxLength(100)]],
  description: ['', [Validators.required, Validators.minLength(20)]],
  location: ['', Validators.required],
  salaryMin: [null, [Validators.required, Validators.min(1)]],
  salaryMax: [null, [Validators.required, Validators.min(1)]]
}, {
  validators: [salaryRangeValidator()]
});
```

## Custom validator

```typescript
export function salaryRangeValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const min = group.get('salaryMin')?.value;
    const max = group.get('salaryMax')?.value;

    if (min != null && max != null && min > max) {
      return { salaryRange: true };
    }

    return null;
  };
}
```

## Senior form practices
- keep form creation in dedicated methods
- map backend validation errors into controls
- avoid huge template logic
- create reusable field components where useful
- disable submit during pending requests

---

# 10. State Management Approaches

Not every app needs NgRx immediately.

## Option 1: Local component state
Good for:
- simple screens
- isolated UI state
- temporary interactions

## Option 2: Service + BehaviorSubject / signals
Good for:
- medium complexity
- shared feature state
- simpler learning curve

## Option 3: NgRx / state library
Good for:
- complex workflows
- many shared states
- auditability
- predictable event-driven state transitions

## Recommendation for this project
Start with:
- feature facade service
- private subject/state
- public readonly observables

## Example facade

```typescript
@Injectable({ providedIn: 'root' })
export class JobsFacade {
  private readonly jobsSubject = new BehaviorSubject<JobResponse[]>([]);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);

  readonly jobs$ = this.jobsSubject.asObservable();
  readonly loading$ = this.loadingSubject.asObservable();

  constructor(private jobsApi: JobsApiService) {}

  loadJobs(filters: JobSearchParams): void {
    this.loadingSubject.next(true);

    this.jobsApi.getJobs(filters).pipe(
      finalize(() => this.loadingSubject.next(false))
    ).subscribe({
      next: response => this.jobsSubject.next(response.content),
      error: () => this.jobsSubject.next([])
    });
  }
}
```

## Senior consideration
As complexity grows, prefer explicit state models over scattered booleans.

---

# 11. UI State vs Server State

Separate these mentally.

## UI state examples
- modal open/closed
- selected tab
- form dirty state
- local sort toggle

## Server state examples
- jobs list from API
- job details
- current user profile
- notifications from backend

## Why separation matters
Server state has:
- loading lifecycle
- stale data concerns
- retries
- caching
- synchronization issues

---

# 12. Change Detection and Performance

Angular performance is often about avoiding unnecessary work.

## Key practices
- use `OnPush` change detection where appropriate
- use `trackBy` in loops
- avoid calling heavy methods in templates
- prefer async pipe over manual subscriptions in templates
- lazy load routes
- split large components

## Example

```typescript
@Component({
  selector: 'app-job-card-list',
  templateUrl: './job-card-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class JobCardListComponent {
  @Input() jobs: JobResponse[] = [];

  trackById(_: number, job: JobResponse): number {
    return job.id;
  }
}
```

## Why `OnPush`
It reduces unnecessary checks and encourages immutable update patterns.

---

# 13. Memory Leak Prevention

A common Angular interview topic.

## Risks
- manual subscriptions never unsubscribed
- long-lived subjects
- event listeners not cleaned up
- nested subscriptions

## Better patterns
- async pipe in templates
- `takeUntilDestroyed()` in components/services where appropriate
- flatten streams instead of nesting subscriptions

## Example

```typescript
@Component({...})
export class JobDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly jobsApi = inject(JobsApiService);

  readonly job$ = this.route.paramMap.pipe(
    map(params => Number(params.get('id'))),
    switchMap(id => this.jobsApi.getJob(id))
  );
}
```

No manual subscription needed in template if consumed with `async`.

---

# 14. Error Handling Strategy

Do not let every component invent its own error handling style.

## Recommended approach
- interceptors for generic HTTP handling
- facade/service for feature-level mapping
- components for display concerns only

## Example backend validation mapping
If backend returns:
```json
{
  "status": 400,
  "error": "Validation Failed",
  "fields": {
    "title": "must not be blank",
    "salaryMin": "must be greater than 0"
  }
}
```

Map it into form errors:

```typescript
applyBackendErrors(form: FormGroup, fields: Record<string, string>): void {
  Object.entries(fields).forEach(([field, message]) => {
    const control = form.get(field);
    if (control) {
      control.setErrors({ backend: message });
    }
  });
}
```

---

# 15. Authentication Flow in Angular

## Typical flow
1. user logs in
2. backend returns access token
3. token stored securely
4. interceptor attaches token
5. guards protect routes
6. logout clears auth state
7. 401 handling redirects or refreshes token

## Storage discussion
### Local storage
Easy, but vulnerable to XSS if app is compromised.

### Memory + refresh token cookie
Safer in many architectures, but more complex.

## Interview note
Be ready to explain frontend token storage trade-offs rather than claiming one universal answer.

---

# 16. Lazy Loading and Route Design

Large apps should not load everything upfront.

## Example route config

```typescript
export const routes: Routes = [
  {
    path: 'jobs',
    loadChildren: () => import('./features/jobs/jobs.routes').then(m => m.JOBS_ROUTES)
  },
  {
    path: 'admin',
    canActivate: [authGuard, employerGuard],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  }
];
```

## Benefits
- smaller initial bundle
- faster startup
- better scalability of feature ownership

---

# 17. Reusable UI Patterns

Create reusable components for repeated patterns:
- loading spinner
- empty state
- error banner
- paginated table
- confirmation dialog
- form field wrapper

## Why this matters
Consistency improves:
- UX
- maintainability
- speed of feature delivery

---

# 18. Testing Strategy

A senior frontend engineer knows what to test and what not to over-test.

## Test these well
- component behavior
- form validation
- service API interactions
- guards
- interceptors
- facade state transitions

## Avoid over-testing
- Angular internals
- trivial getters/setters
- framework behavior already guaranteed

## Example component test

```typescript
describe('JobListComponent', () => {
  let component: JobListComponent;

  beforeEach(() => {
    component = new JobListComponent();
  });

  it('should emit selected job id', () => {
    spyOn(component.selectJob, 'emit');

    component.selectJob.emit(10);

    expect(component.selectJob.emit).toHaveBeenCalledWith(10);
  });
});
```

## Example service test with HttpTestingController

```typescript
describe('JobsApiService', () => {
  let service: JobsApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [JobsApiService]
    });

    service = TestBed.inject(JobsApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should fetch job by id', () => {
    service.getJob(1).subscribe(job => {
      expect(job.id).toBe(1);
    });

    const req = httpMock.expectOne('/api/jobs/1');
    expect(req.request.method).toBe('GET');
    req.flush({ id: 1, title: 'Java Developer' });
  });
});
```

---

# 19. Accessibility and UX Quality

Senior frontend work includes usability, not just rendering.

## Checklist
- semantic HTML
- keyboard navigation
- visible focus states
- ARIA labels where needed
- proper form error messaging
- loading and empty states
- responsive layouts

---

# 20. Common Angular Pitfalls

- too much logic in templates
- subscribing in many places without cleanup
- giant shared services doing everything
- components directly calling many APIs
- mutable state causing confusing UI updates
- no loading/error states
- no route-level code splitting
- duplicated form logic
- weak typing for API contracts

---

# 21. Full-Stack Integration Best Practices

When integrating with Spring Boot:
- define typed request/response interfaces
- centralize API base URL configuration
- normalize backend errors
- align pagination contracts
- align auth token lifecycle
- document assumptions between frontend and backend

## Example interfaces

```typescript
export interface JobRequest {
  title: string;
  description: string;
  location: string;
  salaryMin: number;
  salaryMax: number;
}

export interface JobResponse {
  id: number;
  title: string;
  description: string;
  location: string;
  salaryMin: number;
  salaryMax: number;
}
```

---

# 22. Senior Interview Questions

1. What is the difference between smart and presentational components?
2. When would you use `switchMap` vs `mergeMap` vs `concatMap`?
3. How does Angular change detection work?
4. Why use `OnPush`?
5. How do you prevent memory leaks?
6. What belongs in an interceptor?
7. How do you structure a large Angular application?
8. When do you introduce state management libraries?
9. How do you handle backend validation errors in forms?
10. How do route guards differ from backend authorization?
11. How do you optimize bundle size?
12. How do you test HTTP services?
13. What are common RxJS anti-patterns?
14. How do you design reusable components without over-abstracting?
15. How do you debug inconsistent UI state?

---

# 23. Practice Exercises

## Exercise 1: Refactor Job Feature
Split into:
- page/container component
- list component
- filter component
- API service
- facade

## Exercise 2: Add Auth Flow
Implement:
- login page
- auth service
- token storage
- auth interceptor
- route guard

## Exercise 3: Build Reactive Job Form
Include:
- custom salary validator
- backend error mapping
- loading state
- submit disable logic

## Exercise 4: Add Lazy Loading
Move features into lazy-loaded route groups.

## Exercise 5: Improve Performance
Apply:
- `OnPush`
- `trackBy`
- route-level code splitting
- remove unnecessary subscriptions

## Exercise 6: Add Tests
Write tests for:
- API service
- auth guard
- form validator
- facade state transitions

---

# 24. Suggested Evolution Path

### Stage 1
Basic components + services

### Stage 2
Feature-based structure + interceptors + guards

### Stage 3
Facade/state layer + reusable UI patterns

### Stage 4
Performance tuning + testing maturity + accessibility improvements

---

# 25. Final Advice

Advanced Angular development is not about using every feature of the framework.

It is about building UIs that are:
- understandable
- scalable
- testable
- performant
- accessible
- aligned with backend contracts

If your frontend architecture helps your team move faster without becoming chaotic, you are thinking like a senior engineer.