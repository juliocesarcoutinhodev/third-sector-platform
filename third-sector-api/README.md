# Third Sector API

API REST do sistema de gestão para organizações do terceiro setor, construída com Spring Boot 4,
arquitetura hexagonal + Clean Architecture e modularização via Spring Modulith.

## Stack

| Camada | Tecnologia |
|---|---|
| Runtime | Java 25 + Spring Boot 4 |
| Persistência | PostgreSQL + Spring Data JPA + Flyway |
| Cache | Redis |
| Mensageria | Apache Kafka |
| Armazenamento | MinIO |
| Segurança | Spring Security + JWT (jjwt 0.12.x, HS256) |
| E-mail | Spring Mail + Thymeleaf |
| Mapeamento | MapStruct (+ Lombok @Builder para entidades) |
| Validação | Bean Validation + @CNPJ (Hibernate Validator) |
| Observabilidade | Actuator + Micrometer + Prometheus |
| Testes | JUnit 5 + Testcontainers |
| Arquitetura modular | Spring Modulith |

## Arquitetura

Cada módulo segue arquitetura hexagonal + Clean Architecture leve, com três camadas:

```
municipality/
├── domain/                    ← entidades, value objects, portas (sem dependência de frameworks)
│   ├── Municipality.java
│   ├── Plan.java
│   ├── DuplicateSubdomainException.java
│   ├── MunicipalityNotFoundException.java
│   └── port/out/
│       └── MunicipalityRepository.java     ← porta de saída (interface)
├── application/               ← casos de uso (um por classe)
│   ├── RegisterMunicipalityUseCase.java
│   ├── FindMunicipalityBySubdomainUseCase.java
│   ├── FindMunicipalityByIdUseCase.java
│   ├── ListActiveMunicipalitiesUseCase.java
│   └── MunicipalityView.java              ← DTO de saída
├── adapter/                   ← dependem de domain/application, nunca o contrário
│   ├── in/web/
│   │   ├── MunicipalityController.java    ← controller fino, só delega aos use cases
│   │   └── RegisterMunicipalityRequest.java ← DTO de entrada com validação
│   └── out/persistence/
│       ├── MunicipalityEntity.java        ← entidade JPA
│       ├── MunicipalityPersistenceAdapter.java ← implementa a porta
│       └── SpringDataMunicipalityRepository.java
└── package-info.java          ← @ApplicationModule
```

### Regras de arquitetura

- **Domínio nunca depende de Spring, JPA ou qualquer framework.** Apenas Java puro.
- **Um caso de uso = uma classe** para lógica não trivial. Operações CRUD simples podem usar `*Service`,
  mas o padrão é use case.
- **Comunicação entre módulos**: Spring `ApplicationEvent` / `@ApplicationModuleListener`,
  `@NamedInterface` para chamadas diretas, ou Kafka para durabilidade/retry.
- **Shared é um módulo OPEN** — todos os módulos podem importar dele livremente.
  Contém exceções base, wrappers de resposta (`ApiResponse`, `ErrorResponse`, `PageResponse`)
  e o `GlobalExceptionHandler`.

## Módulos

```
br.com.toponesystem.thirdsector
├── shared/                ← módulo OPEN: exceções base, ErrorResponse, GlobalExceptionHandler
│   ├── domain/exception/  ← BusinessException, ConflictException, ResourceNotFoundException
│   └── adapter/in/web/    ← ApiResponse, ErrorResponse, PageResponse, GlobalExceptionHandler,
│                             SecurityConfig, RequestIdFilter, OpenApiConfig, EnvFileLogger
├── tenant/                ← multi-tenancy (schema-per-tenant, TenantFilter, migrações)
│   ├── domain/
│   │   ├── TenantContext.java       ← core: ThreadLocal holder do tenant atual
│   │   ├── exception/               ← TenantContextNotSetException
│   │   ├── model/                   ← IsolationRecord (entidade de validação de isolamento)
│   │   └── port/out/                ← TenantValidator (porta de validação)
│   ├── adapter/
│   │   ├── in/web/TenantFilter      ← resolve subdomínio → TenantContext
│   │   ├── in/async/                ← AsyncConfiguration, TaskDecorator
│   │   └── out/{persistence, migration, validation}
│   └── config/TenantProperties.java
├── municipality/          ← cadastro de municípios (CRUD completo com hexagonal)
│   ├── domain/{model, exception, port/out}
│   ├── application/{MunicipalityView, usecase/}
│   └── adapter/{in/web, out/persistence}
 ├── auth/                  ← domínio User/Role, persistência, testes (sem controllers/use cases)
├── organization/          ← domínio Organization, CreateOrganizationUseCase, persistência, testes
├── notification/          ← Kafka (producer/consumer), Spring Mail, Thymeleaf templates, testes
├── financial/             ← stub
└── transparency/          ← stub
```

## Respostas da API

### Success — `ApiResponse<T>`

Toda resposta de sucesso é envelopada em `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Município cadastrado com sucesso.",
  "data": {
    "id": 1,
    "name": "Maringá",
    "cnpj": "11222333000181",
    "subdomain": "maringa",
    "plan": "BASIC",
    "active": true,
    "createdAt": "2026-06-20T20:00:00Z",
    "updatedAt": "2026-06-20T20:00:00Z"
  }
}
```

### Error — `ErrorResponse`

Erros são tratados exclusivamente pelo `GlobalExceptionHandler` (`@RestControllerAdvice`).
**Controllers nunca** tratam exceções — apenas lançam. O handler mapeia exceções tipadas
para `ErrorResponse` com HTTP status apropriado.

**Hierarquia de exceções:**

```
RuntimeException
├── ResourceNotFoundException  (shared) → 404
│   ├── MunicipalityNotFoundException (municipality)
│   └── OrganizationNotFoundException (organization)
├── ConflictException          (shared) → 409
│   ├── DuplicateSubdomainException (municipality)
│   └── DuplicateCnpjException (organization)
├── BusinessException          (shared) → 422
│   ├── InvalidUserRoleAssignmentException (auth)
│   └── EmailSendFailedException (notification)
└── Exception                  → 500
```

**Exemplo 404:**
```json
{
  "success": false,
  "status": 404,
  "error": "Not Found",
  "message": "Municipality with subdomain 'x' not found",
  "timestamp": "2026-06-20T20:00:00Z"
}
```

**Exemplo 400 — validação:**
```json
{
  "success": false,
  "status": 400,
  "error": "Bad Request",
  "message": "Erro de validação.",
  "errors": [
    {"field": "cnpj", "message": "invalid Brazilian taxpayer registry number"},
    {"field": "subdomain", "message": "must contain only lowercase letters, numbers, and hyphens"}
  ],
  "timestamp": "2026-06-20T20:00:00Z"
}
```

## Módulos — visão detalhada

### Municipality (referência canônica)

Módulo mais completo — serve de referência para os demais. Implementa CRUD completo
com validação de CNPJ via `@CNPJ` (Hibernate Validator), strip de máscara no use case,
e expõe API cross-module (`MunicipalityDataProvider`) consumida pelo módulo `notification`
para branding de e-mails (nome e logo da prefeitura).

**Padrão de mapeamento via MapStruct:** entidades JPA precisam de `@Builder`
(Lombok) para que o MapStruct consiga construir instâncias sem setters públicos.
O mapper (`*EntityMapper`) é injetado no `*PersistenceAdapter` e contém apenas
`toEntity()` / `toDomain()` — zero lógica de negócio. Exemplo: `OrganizationEntityMapper`.

### Auth

Domínio de autenticação com `User`, binding de roles e endpoint de login:

**Roles e binding:**

| Role | `organizationId` |
|---|---|
| `SUPER_ADMIN` | deve ser `null` |
| `MUNICIPALITY_ADM` | deve ser `null` |
| `ORGANIZATION_MANAGER` | obrigatório |
| `OPERATOR` | obrigatório |

A validação role-organization ocorre no factory method `User.create()`.
O construtor de hidratação (all-args público) não valida — permite
reconstruir qualquer estado vindo da persistência.

**Endpoints:**

| Método | Path | Descrição |
|---|---|---|
| `POST` | `/api/users` | Cadastro de usuário (BCrypt, evento `UserRegisteredEvent`) |
| `POST` | `/api/auth/login` | Login com email/senha, retorna JWT em cookie HttpOnly |

**Login:**
- `LoginUseCase` valida credenciais + usuário ativo, retorna erro genérico
  (`AuthenticationFailedException`) em todos os casos de falha (evita enumeração de emails)
- `JjwtTokenGenerator` assina JWT HS256 com claims: `sub` (userId), `role`, `tenantId`, `organizationId`
- Cookie `access_token`: HttpOnly, Secure (configurável por profile), SameSite=Lax, maxAge = expiração
- Configuração: `security.jwt.secret` (env `JWT_SECRET`), `security.jwt.expiration` (default 15min)
- Resposta NUNCA contém o token no body — apenas dados não sensíveis do usuário

**Pendente:** `UserDetailsService`, JWT verification filter, refresh token rotation,
CSRF protection, `@PreAuthorize` nos endpoints.

### Organization

Entidade de domínio representando ONGs/entidades do terceiro setor:

| Campo | Descrição |
|---|---|
| `name` | Nome da organização |
| `cnpj` | 14 dígitos sem máscara, validado via `@CNPJ` |
| `status` | `PENDING` (criação), `ACTIVE`, `SUSPENDED` |

`CreateOrganizationUseCase` faz strip da máscara CNPJ e persiste com status `PENDING`.
Mapeamento `Organization` ↔ `OrganizationEntity` via MapStruct com builder
(`OrganizationEntityMapper` + `@Builder` na entidade). Tabela `organizations`
no schema tenant com constraint `UNIQUE(cnpj)`.

**Pendente:** fluxo completo de cadastro público, upload de documentos,
aprovação pelo ADM, notificações.

### Notification

Infraestrutura de e-mail assíncrono:

| Componente | Função |
|---|---|
| `EmailNotification` | Modelo imutável (Jackson-serializable para Kafka) |
| `NotificationPublisher` | Porta de saída → `EmailNotificationProducer` (Kafka) |
| `EmailSender` | Porta de saída → `SpringMailEmailSender` (JavaMailSender) |
| `EmailTemplateRenderer` | Porta de saída → `ThymeleafEmailTemplateRenderer` |

Fluxo: producer serializa `EmailNotification` como JSON → tópico Kafka
`notification.email` (3 partições, RF 1) → consumer desserializa, resolve
município via `MunicipalityDataProvider`, renderiza template Thymeleaf
(conteúdo + layout `base.html` com logo/nome da prefeitura) e dispara e-mail.

O consumer usa `group-id` randômico (UUID) para que toda instância
receba todas as mensagens. Templates em `mail-templates/` com Thymeleaf
`SpringTemplateEngine` dedicado (não conflita com templates web).

## Pré-requisitos

- Java 25+
- Maven 3.9+
- Docker e Docker Compose (para infraestrutura local)

## Configuração local

### 1. Infraestrutura e variáveis de ambiente

As variáveis de ambiente e a infraestrutura local são gerenciadas centralmente em `../infra/`.

```bash
cd ../infra
cp .env.example .env
# edite .env com suas credenciais
docker compose up -d
```

Aguarde todos os serviços aparecerem como `healthy`:

```bash
docker compose ps
```

A API lê as variáveis automaticamente de `../infra/.env` no perfil `dev`
— não é necessário nenhum `.env` dentro desta pasta.

### 2. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080` com o perfil `dev`.

## Perfis

| Perfil | Uso | Logs | Formato |
|---|---|---|---|
| `dev` | Desenvolvimento local | DEBUG (app), padrão (libs) | JSON (logstash) |
| `test` | Testes automatizados | WARN | texto |
| `prod` | Produção | INFO (app), WARN (libs) | JSON (logstash) |

## Variáveis de ambiente

Todas as credenciais sensíveis são injetadas via variáveis de ambiente
— nenhum valor sensível é hardcoded.

Consulte `../infra/.env.example` para a lista completa e documentada.

## Schema e migrações

O schema do banco é versionado pelo Flyway. As migrations são separadas por escopo:

| Pasta | Schemas gerenciados |
|---|---|
| `db/migration/master` | Schema `master` (dados cross-tenant: municípios, planos, config) |
| `db/migration/tenant` | Schemas individuais de cada município (dados operacionais) |

### Master

| Versão | Descrição |
|---|---|
| V1 | Criação do schema `master` |
| V2 | Tabela `municipality` (nome, cnpj, subdomain, plan, active) |
| V3 | Expansão da tabela `municipality` (name, cnpj, subdomain, plan, active, timestamps) |
| V4 | Coluna `cnpj` armazenada apenas com dígitos (VARCHAR 14) |
| V5 | Tabela `super_admin` (name, email, password_hash, active, timestamps) |
| V6 | Coluna `logo` na tabela `municipality` (VARCHAR 500) |

### Tenant

| Versão | Descrição |
|---|---|
| V1 | Baseline do schema tenant |
| V2 | Tabela `isolation_record` — validação de isolamento multi-tenant |
| V3 | Tabela `users` (name, email, password_hash, role, organization_id, active, timestamps) |
| V4 | Tabela `organizations` (name, cnpj, status, timestamps) — UNIQUE em cnpj |
| V5 | Tabela `event_publication` — registro de eventos do Spring Modulith |

No startup, o `TenantMigrationStartupRunner` aplica as migrations do master e depois
itera sobre todos os municípios ativos aplicando as migrations nos schemas tenant.

Para inspecionar o histórico de migrações:

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## Arquitetura modular

Os limites entre módulos são verificados automaticamente pelo Spring Modulith.
Para rodar a verificação estrutural:

```bash
./mvnw test -Dtest=ModularityTests
```

A documentação gerada (diagramas C4) fica em `target/spring-modulith-docs/`
após rodar `writeDocumentation()`.

## Logs estruturados

Nos perfis `dev` e `prod`, os logs são emitidos em JSON (formato logstash).
Cada linha inclui:

- `@timestamp`, `level`, `logger`, `message`
- `requestId` — UUID único por requisição HTTP
- `tenantId` — identificador da prefeitura (populado pelo TenantFilter)

Exemplo:

```json
{
  "@timestamp": "2026-06-20T14:00:00.000Z",
  "level": "INFO",
  "logger": "br.com.toponesystem.thirdsector.shared.adapter.in.web.EnvFileLogger",
  "message": "Loaded environment from: \"../infra/.env\"",
  "requestId": "a1b2c3d4-...",
  "tenantId": ""
}
```

## Documentação da API

| Recurso | URL | Perfis |
|---|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html | `dev`, `test` |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | `dev`, `test` |

Em `prod` ambos os endpoints retornam 404 — a documentação não é exposta publicamente.

## Testes

```bash
# todos os testes
./mvnw test

# verificação de arquitetura modular
./mvnw test -Dtest=ModularityTests
```

Os testes de integração rodam contra uma instância real do PostgreSQL via container efêmero
(Testcontainers). Não é necessário ter o `docker-compose` da pasta `infra/` rodando.

### Classe base de integração

`AbstractIntegrationTest` é a classe base para todos os testes que precisam de banco de dados real.
Ela declara o container PostgreSQL como `static`, garantindo que **um único container é compartilhado**
por toda a suíte — não há reinicialização entre classes de teste.

```java
class MeuTesteDeIntegracao extends AbstractIntegrationTest {
    @Test
    void meuTeste() { ... }
}
```

O container sobe na primeira vez que qualquer classe que estende `AbstractIntegrationTest`
é carregada, e é destruído automaticamente ao final da suíte. As propriedades
`spring.datasource.*` são injetadas dinamicamente via `@DynamicPropertySource`,
isolando completamente o ambiente de testes do ambiente de desenvolvimento.

### Teste de isolamento multi-tenant

`TenantDataIsolationTest` valida o fluxo completo de multi-tenancy sem mocks:

1. Cadastra dois municípios via `RegisterMunicipalityUseCase`
2. Executa `TenantMigrationService` para criar os schemas tenant
3. Insere dados distintos em cada schema via `IsolationRecordRepository`
4. Alterna `TenantContext` e prova que tenant A nunca vê dados de tenant B
5. Sem tenant definido → query falha (tabela não existe no schema `master`)

```java
class TenantDataIsolationTest extends AbstractIntegrationTest {
    // 4 assertions provando isolamento total entre schemas
}
```

## Observabilidade

### Endpoints do Actuator

A exposição dos endpoints varia por perfil — endpoints sensíveis ficam fechados em produção:

| Endpoint | `dev` / `test` | `prod` |
|---|---|---|
| `GET /actuator/health` | detalhes completos | status apenas |
| `GET /actuator/info` | sim | sim |
| `GET /actuator/env` | sim | nao |
| `GET /actuator/beans` | sim | nao |
| `GET /actuator/mappings` | sim | nao |
| `GET /actuator/metrics` | sim | nao |
| `GET /actuator/prometheus` | sim | nao |

### Prometheus

O endpoint `/actuator/prometheus` expõe métricas no formato Prometheus
(JVM, HTTP requests, connection pool, etc.).

O Prometheus local sobe junto com a infra via Docker Compose e já está configurado
para fazer scrape da API em `host.docker.internal:8080`. A UI fica em http://localhost:9090.

Queries úteis:
```promql
up
http_server_requests_seconds_count
jvm_memory_used_bytes
hikaricp_connections_active
```

### Grafana

O Grafana sobe junto com a infra e já contém o datasource Prometheus e o dashboard
**Third Sector API — Overview** provisionados automaticamente — sem configuração manual na UI.

| Painel | Métrica |
|---|---|
| JVM Heap Memory | Uso e limite da heap por geração |
| CPU Usage | CPU do processo e do sistema |
| HTTP Request Rate | Taxa de requisições por endpoint/status |
| HTTP Latency p99 / p50 | Percentis de latência por endpoint |
| HikariCP Connections | Conexões ativas, idle e pending |
| HTTP 5xx Error Rate | Taxa de erros 5xx por endpoint |

Acesse http://localhost:3000 (credenciais do `infra/.env`).
