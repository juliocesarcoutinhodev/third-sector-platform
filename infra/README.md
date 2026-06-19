# Infraestrutura Local — Third Sector Platform

Ambiente de desenvolvimento completo via Docker Compose. Sobe PostgreSQL, Redis, Kafka, MinIO e Mailpit com um único comando.

## Serviços

| Serviço | Porta(s) | Interface |
|---|---|---|
| PostgreSQL | 5432 | — |
| Redis | 6379 | — |
| Kafka | 9092 | — |
| MinIO | 9000 (API) / 9001 (console) | http://localhost:9001 |
| Mailpit | 1025 (SMTP) / 8025 (UI) | http://localhost:8025 |

## Pré-requisitos

- Docker Engine 24+
- Docker Compose v2

## Configuração

```bash
cp .env.example .env
```

Edite o `.env` com suas credenciais locais. Para o `KAFKA_CLUSTER_ID`, gere um ID único:

```bash
docker run --rm apache/kafka:3.9.0 /opt/kafka/bin/kafka-storage.sh random-uuid
```

## Comandos

### Subir todos os serviços
```bash
docker compose up -d
```

### Verificar status e healthchecks
```bash
docker compose ps
```
Aguarde todos os serviços aparecerem como `healthy` antes de iniciar a API.

### Ver logs em tempo real
```bash
docker compose logs -f
```

### Ver logs de um serviço específico
```bash
docker compose logs -f postgres
docker compose logs -f kafka
```

### Derrubar os serviços (mantém volumes)
```bash
docker compose down
```

### Derrubar e apagar todos os volumes (reset completo)
```bash
docker compose down -v
```

### Reiniciar um serviço específico
```bash
docker compose restart postgres
```

## Criação de bucket no MinIO

Após subir os serviços, crie o bucket manualmente pelo console em http://localhost:9001 (credenciais do `.env`).

> Automação via script de inicialização está fora do escopo desta story — será endereçada futuramente.

## Volumes

| Volume | Serviço | Dados |
|---|---|---|
| `postgres_data` | PostgreSQL | Banco de dados |
| `kafka_data` | Kafka | Tópicos e offsets |
| `minio_data` | MinIO | Objetos armazenados |
