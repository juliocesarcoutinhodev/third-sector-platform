# Third Sector API — Agent Guidelines

This is the backend API of a SaaS platform for the Brazilian third sector (nonprofits, NGOs,
associations). The business model is B2G: municipalities (prefeituras) license the platform to
manage financial transparency and public fund reporting for organizations that receive public
transfers. There is also a B2B fallback (selling directly to organizations).

The platform has three components in this monorepo, but this file governs only the backend API
(`third-sector-api`):
- `third-sector-api` — Spring Boot backend (this project)
- `third-sector-admin` — Angular + PrimeNG/Sakai admin frontend (separate project)
- `third-sector-transparency` — Angular SSR public transparency portal (separate project)

## Non-negotiable architectural rules

1. **Hexagonal architecture + lightweight Clean Architecture.** Every module is organized in
   three layers: `domain`, `application`, `adapter` (with `adapter/in/web` and
   `adapter/out/persistence` at minimum). Domain logic must never depend on Spring, JPA, or any
   framework annotation. Adapters depend on domain/application, never the other way around.
2. **No heavy DDD.** Do not introduce Aggregate Roots, generic Repository abstractions, or Value
   Objects for every primitive. Use Value Objects only where it truly matters (e.g. `Cnpj`,
   `Money`), not everywhere. Spring Data JPA repositories can be used directly in the `adapter/out`
   layer — do not wrap them behind another abstraction "just in case."
3. **One use case = one class** for non-trivial application logic (e.g. `ApproveEntryUseCase`,
   `CreateMunicipalityUseCase`). Simple CRUD-like operations can live in a straightforward
   `*Service` class — don't force a use-case class for trivial operations.
4. **Use cases NEVER accept a list of loose primitive parameters.** Any use case taking more
   than one argument must receive a single typed input object (a `Command` or `Input` record,
   e.g. `CreateUserCommand`, `ApproveEntryCommand`), defined in the `application` layer — never
   `execute(String name, String email, String password, Role role, Long organizationId)`. This
   applies regardless of how few parameters exist today: a method with 3 primitive parameters
   grows into 10, then 30, and positional primitive arguments of the same type (e.g. two
   `String` fields) are a silent bug risk if ever reordered, with no compiler safety net.
   Controllers map their incoming request DTO into this Command via MapStruct (see the mapping
   rule below) — never by manually unpacking `request.field()` calls into a multi-argument method
   call.
5. **Modular monolith via Spring Modulith.** The application is organized into modules:
   `tenant`, `auth`, `municipality`, `organization`, `financial`, `notification`, `transparency`,
   and a minimal `shared` kernel. Modules must NEVER import internal classes from another module
   directly. Cross-module communication happens through:
   - Spring `ApplicationEvent` / `@ApplicationModuleListener` for in-process events
   - Explicit `@NamedInterface`-exposed APIs when a direct call is unavoidable
   - Kafka topics, when durability/retry across module boundaries is required (e.g. notifications)
   Every module package must have a `package-info.java`. Any change must keep
   `ApplicationModules.of(...).verify()` passing — never weaken or skip this test to make code
   compile.
6. **Multi-tenancy is schema-per-tenant**, one PostgreSQL schema per municipality. The tenant
   concept maps to the `Municipality` entity. Tenant resolution flows through:
   `TenantFilter` (resolves from subdomain, never trusts client-supplied headers in prod) →
   `TenantContext` (ThreadLocal) → Hibernate's `CurrentTenantIdentifierResolver` /
   `MultiTenantConnectionProvider`. Async code (`@Async`, Modulith listeners) MUST go through the
   `TaskDecorator` that propagates `TenantContext` — never assume a new thread inherits tenant
   context automatically. A `master` schema holds cross-tenant data only (municipalities, plans).
    Never let a query silently fall back to a default schema when no tenant is set — fail fast.
6. **Use Command records for Use Case inputs.** Every use case that creates or mutates data MUST
   receive a single `record` (e.g. `CreateUserCommand`, `RegisterMunicipalityCommand`) instead
   of multiple primitive parameters. The adapter layer uses MapStruct mappers
   (`*Request → *Command`) to convert request DTOs into Commands. The controller becomes:
   validate request → `mapper.toCommand(request)` → `useCase.execute(command)` → return response.
   Read-only queries (`findById`, `findBySubdomain`) with a single primitive parameter are exempt.

## Naming conventions

- **All code is in English**: package names, class names, method names, variable names, commit
  messages in code comments. Only user-facing UI strings (which don't live in this backend) are
  in Portuguese — error messages returned by the API to be displayed to end users may be in
  Portuguese if they are pure text content, but field names, enum values, and code stay in English.
- The domain entity representing a nonprofit/NGO is called **`Organization`**, never `Entity`
  (avoids collision with the JPA `@Entity` concept).
- Base package: `com.yourproduct.thirdsector` (adjust to whatever the real groupId/package is in
  this repo — check `build.gradle`/`pom.xml` before assuming).
- Module package structure example:
  ```
  com.yourproduct.thirdsector.financial
  ├── domain/
  ├── application/
  ├── adapter/
  │   ├── in/web/
  │   └── out/persistence/
  └── package-info.java
  ```

## Mapping rules (MapStruct)

- **MapStruct is used for all structural object-to-object mapping**: `Entity <-> Domain model`,
  `Request DTO -> Command/Input`, `Domain model/View -> Response DTO`. Every adapter or
  controller that needs this kind of translation must define a MapStruct `@Mapper` interface
  for it — never write `toEntity`/`toDomain`/`toCommand` methods by hand inside an adapter,
  controller, or service class.
- **MapStruct mappers contain ZERO business logic.** They only translate structurally similar
  objects. The moment a mapping requires a decision (e.g. "if status is X, compute Y", any
  conditional, any validation, any side effect), that logic does NOT belong in a `default`
  method inside the mapper — it belongs in the domain model or the use case. The mapper only
  translates the already-decided result.
- Configure mappers with `unmappedTargetPolicy = ReportingPolicy.ERROR` (or the project's agreed
  equivalent) so that a new field added to either side of a mapping causes a compile-time
  failure instead of a silently dropped field at runtime.

## Tech stack — use these, don't substitute without asking

- **Framework**: Spring Boot, Spring Security, Spring Data JPA, Spring Validation, Spring Mail,
  Spring Actuator, Spring Web, Spring Modulith
- **Database**: PostgreSQL, Flyway (separate migration sets for `master` schema vs tenant
  schemas — check the existing `db/migration/master` and `db/migration/tenant` folder convention
  before adding a new migration)
- **Mapping/boilerplate**: MapStruct (use mainly at module boundaries, between domain and
  DTOs — don't force mapping between internal domain VOs), Lombok
- **Messaging**: Kafka (used for durable, retry-safe cross-module notifications — e.g. user
  registered, entry approved — not for every event; in-process Spring events are fine for
  same-transaction-boundary concerns)
- **Caching**: Redis
- **File storage**: MinIO (always access via short-lived presigned URLs, never expose the bucket
  publicly)
- **Reporting**: Jasper Reports (`.jrxml` files live in their own module/folder, kept separate
  from business logic)
- **Testing**: Testcontainers for integration tests (PostgreSQL is mandatory for any test
  touching multi-tenancy or schema behavior — never use H2 for these, it doesn't replicate
  PostgreSQL schema semantics)
- **Docs**: springdoc-openapi (Swagger UI), enabled only in `dev`/`test`, disabled in `prod`
  (`springdoc.api-docs.enabled=false` and `springdoc.swagger-ui.enabled=false` in
  `application-prod.yml`)
- **Observability**: Micrometer + Prometheus (`/actuator/prometheus`, same prod-disable rule as
  Swagger), Grafana, Loki + Promtail consuming the JSON structured logs (see below)
- **Auth**: Spring Security with HttpOnly cookies, refresh token rotation, and reuse detection
  (revoke the whole token family on detected replay). CSRF protection required since cookies are
  used. Never store JWTs in localStorage/sessionStorage.

## Logging

- Logback configured for **JSON structured output** (`logstash-logback-encoder` or equivalent).
- Every log line must carry `tenantId` and `requestId` via MDC when available.
- `MDC.clear()` (or equivalent try/finally cleanup) is mandatory at the end of every request and
  every async task — never let MDC values leak across reused threads.
- Log levels differ by profile: `dev` = DEBUG (app) / INFO (libs), `test` = WARN, `prod` = INFO
  (app) / WARN (libs).

## Testing expectations

- Any feature touching multi-tenancy, schema resolution, or cross-tenant data access MUST have
  an integration test using Testcontainers that proves isolation (tenant A never sees tenant B's
  data), not just a unit test with mocks.
- Async/event-driven code (Modulith listeners, Kafka consumers) must have a test that proves
  `TenantContext` propagation actually works in the async thread, not just that the method runs.
- Don't add Testcontainers modules (e.g. Kafka) speculatively before there's a real producer or
  consumer to test — infrastructure for tests is added alongside the first feature that needs it,
  not as a separate "setup" story.

## CI/CD

- GitHub Actions workflow for this project is `ci-api.yml` at the monorepo root
  `.github/workflows/`, with `paths: ['third-sector-api/**']` so it doesn't trigger on changes to
  the other two projects in the monorepo.
- Do not assume a CD/deploy pipeline exists yet unless told otherwise — check before referencing
  one in any generated config or documentation.

## Jira / story format (for reference, not something the agent edits)

Stories in this project follow this structure: User Story (As a / I want / So that) →
Description → Acceptance Criteria → Technical Tasks → Out of Scope. When asked to implement "story
X.Y", look for this structure to understand full scope and explicit out-of-scope boundaries —
don't implement something explicitly marked out of scope for that story without being asked.

## What to do when something isn't covered here

If a decision isn't covered by this file (e.g. a new module's internal structure, a new
dependency choice), default to the architectural principles above (hexagonal layering, thin
modules, no framework leakage into domain code) and flag the assumption explicitly in your
response rather than silently picking an approach that contradicts the existing codebase
patterns.  

## Observational notes
Always consult the documentation on the web to validate new features, and check whether the implementation is in accordance with the technologies used.