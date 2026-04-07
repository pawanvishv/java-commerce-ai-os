COMPOSE=docker compose -f infra/docker-compose/docker-compose.yml
COMPOSE_LIGHT=docker compose -f infra/docker-compose/docker-compose.dev-light.yml

.PHONY: infra-up infra-down infra-ps infra-logs infra-clean infra-light infra-light-down build test compile

## Start all infrastructure
infra-up:
	$(COMPOSE) up -d

## Stop all infrastructure
infra-down:
	$(COMPOSE) down

## Show running containers
infra-ps:
	$(COMPOSE) ps

## Tail logs (usage: make infra-logs s=kafka)
infra-logs:
	$(COMPOSE) logs -f $(s)

## Destroy everything including volumes
infra-clean:
	$(COMPOSE) down -v --remove-orphans

## Start lightweight infra (Postgres, Redis, Kafka, MongoDB only)
infra-light:
	$(COMPOSE_LIGHT) up -d

## Stop lightweight infra
infra-light-down:
	$(COMPOSE_LIGHT) down

## Stop heavy non-essential containers
infra-slim:
	$(COMPOSE) stop opensearch opensearch-dashboards temporal temporal-ui kafka-ui pgadmin

## Start only essential containers
infra-essential:
	$(COMPOSE) start postgres redis zookeeper kafka mongodb

## Build all services
build:
	mvn clean package -DskipTests

## Run all tests
test:
	mvn test

## Compile only
compile:
	mvn compile -DskipTests

## Install all shared modules
install-shared:
	mvn clean install -pl shared/common-models,shared/avro-schemas,shared/kafka-contracts,shared/tenant-context,shared/resilience-config,shared/test-utils -DskipTests

## Create all databases
create-dbs:
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE auth_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE platform_admin_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE provisioning_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE notification_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE tax_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE billing_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE payment_adapter_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE ledger_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE inventory_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE catalog_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE order_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE seller_db;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE temporal;" 2>/dev/null || true
	docker exec commerce-postgres psql -U commerce -d postgres -c "CREATE DATABASE temporal_visibility;" 2>/dev/null || true
	@echo "All databases created"