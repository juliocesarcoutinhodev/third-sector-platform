# Third Sector API

API REST do sistema de gestão para organizações do terceiro setor, construída com Spring Boot 4 e arquitetura modular.

## Stack

| Camada | Tecnologia |
|---|---|
| Runtime | Java 25 + Spring Boot 4 |
| Persistência | PostgreSQL + Spring Data JPA + Flyway |
| Cache | Redis |
| Mensageria | Apache Kafka |
| Armazenamento | MinIO |
| Segurança | Spring Security + JWT |
| E-mail | Spring Mail |
| Mapeamento | MapStruct |
| Observabilidade | Actuator + Micrometer + Prometheus |
| Testes | JUnit 5 + Testcontainers |
| Arquitetura modular | Spring Modulith |

## Módulos

```
br.com.toponesystem.thirdsector
├── auth            # Autenticação e autorização (JWT)
├── tenant          # Gestão de tenants (multi-tenant)
├── municipality    # Municípios e dados públicos
├── organization    # Organizações do terceiro setor
├── financial       # Gestão financeira
├── notification    # Notificações (e-mail, push)
├── transparency    # Portal de transparência
└── shared          # Utilitários e componentes compartilhados
```

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

A API lê as variáveis automaticamente de `../infra/.env` no perfil `dev` — não é necessário nenhum `.env` dentro desta pasta.

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

Todas as credenciais sensíveis são injetadas via variáveis de ambiente — nenhum valor sensível é hardcoded.

Consulte `../infra/.env.example` para a lista completa e documentada.

## Schema e migrações

O schema do banco é versionado pelo Flyway. As migrations ficam em `src/main/resources/db/migration/`.

| Versão | Descrição |
|---|---|
| V1 | Criação do schema `master` (cross-tenant: municípios, planos, configurações globais) |

Para inspecionar o histórico de migrações aplicadas:

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## Arquitetura modular

Os limites entre módulos são verificados automaticamente pelo Spring Modulith. Para rodar a verificação estrutural:

```bash
./mvnw test -Dtest=ModularityTests
```

A documentação gerada (diagramas C4) fica em `target/spring-modulith-docs/` após rodar `writeDocumentation()`.

## Logs estruturados

Nos perfis `dev` e `prod`, os logs são emitidos em JSON (formato logstash). Cada linha inclui:

- `@timestamp`, `level`, `logger`, `message`
- `requestId` — UUID único por requisição HTTP
- `tenantId` — identificador da prefeitura (populado pelo TenantFilter a partir do Épico 1)

Exemplo de saída:

```json
{
  "@timestamp": "2026-06-19T14:00:00.000Z",
  "level": "INFO",
  "logger": "br.com.toponesystem.thirdsector.shared.config.EnvFileLogger",
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

Os testes de integração rodam contra uma instância real do PostgreSQL via container efêmero (Testcontainers). Não é necessário ter o `docker-compose` da pasta `infra/` rodando.

### Classe base de integração

`AbstractIntegrationTest` é a classe base para todos os testes que precisam de banco de dados real. Ela declara o container PostgreSQL como `static`, garantindo que **um único container é compartilhado** por toda a suíte — não há reinicialização entre classes de teste.

```java
class MinhaIntegrationTest extends AbstractIntegrationTest {
    @Test
    void meuTeste() { ... }
}
```

O container sobe na primeira vez que qualquer classe que estende `AbstractIntegrationTest` é carregada, e é destruído automaticamente ao final da suíte. As propriedades `spring.datasource.*` são injetadas dinamicamente via `@DynamicPropertySource`, isolando completamente o ambiente de testes do ambiente de desenvolvimento.

## Observabilidade

### Endpoints do Actuator

A exposição dos endpoints varia por perfil — endpoints sensíveis ficam fechados em produção:

| Endpoint | `dev` / `test` | `prod` |
|---|---|---|
| `GET /actuator/health` | ✅ detalhes completos | ✅ status apenas |
| `GET /actuator/info` | ✅ | ✅ |
| `GET /actuator/env` | ✅ | ❌ 404 |
| `GET /actuator/beans` | ✅ | ❌ 404 |
| `GET /actuator/mappings` | ✅ | ❌ 404 |
| `GET /actuator/metrics` | ✅ | ❌ 404 |

### Resposta de exemplo

**`GET /actuator/health`** (dev):
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

**`GET /actuator/info`**:
```json
{
  "app": {
    "description": "API de gestão para organizações do terceiro setor"
  },
  "build": {
    "artifact": "third-sector-api",
    "version": "0.0.1-SNAPSHOT",
    "time": "2026-06-19T15:00:00Z"
  }
}
```

A versão em `build.version` é populada automaticamente pelo Maven via o goal `build-info` do plugin Spring Boot.
