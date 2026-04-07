# Commerce OS — Smart AI-Powered Multi-Tenant Commerce Platform

Java 21 + Spring Boot | 13 Services | 5 Phases

## Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- Docker Desktop (6GB RAM)

### Start Infrastructure
```bash
make infra-light
make create-dbs
```

### Install Shared Modules
```bash
make install-shared
```

### Start Services
```bash
mvn spring-boot:run -pl platform/auth-service
mvn spring-boot:run -pl platform/api-gateway
mvn spring-boot:run -pl platform/platform-admin-service
mvn spring-boot:run -pl commerce/tax-calculation-service
mvn spring-boot:run -pl commerce/ledger-service
mvn spring-boot:run -pl commerce/inventory-service
mvn spring-boot:run -pl commerce/catalog-service
mvn spring-boot:run -pl commerce/order-service
mvn spring-boot:run -pl marketplace/seller-service
```

## Service Ports

| Service | Port |
|---|---|
| api-gateway | 8080 |
| auth-service | 8081 |
| platform-admin-service | 8082 |
| provisioning-service | 8083 |
| notification-service | 8084 |
| tax-calculation-service | 8085 |
| payment-adapter-service | 8086 |
| ledger-service | 8087 |
| inventory-service | 8089 |
| catalog-service | 8091 |
| order-service | 8092 |
| seller-service | 8093 |
| billing-sidecar | 8200 |
| inventory-sidecar | 8201 |
| order-sidecar | 8202 |

## Infrastructure Ports

| Service | Port |
|---|---|
| PostgreSQL | 5432 |
| Redis | 6379 |
| Kafka | 9092 |
| MongoDB | 27017 |
| OpenSearch | 9200 |
| Temporal | 7233 |
| Kafka UI | 8090 |
| Temporal UI | 8088 |
| pgAdmin | 5050 |

## Get JWT Token
```bash
curl -X POST "http://localhost:8081/api/v1/auth/token?clientId=test-client-001&clientSecret=secret123"
```

## Daily Development Workflow
```bash
# 1. Start Docker Desktop
# 2. Start infra
make infra-light
make create-dbs
# 3. Start services you need in IntelliJ or terminal
```

## Architecture
- Phase 0: Platform foundation (auth, gateway, admin, provisioning, notification)
- Phase 1: Core commerce (tax, billing, payment, ledger, inventory, catalog, order)
- Phase 2: Operations wiring (Kafka flows, reconciliation, GRN)
- Phase 3: AI sidecars (billing invoice, inventory reorder, driver assignment)
- Phase 4: Marketplace (seller KYC, commission, payout)
- Phase 5: Scale (multi-currency, archival, credential rotation)
