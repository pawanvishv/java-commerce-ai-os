#!/bin/bash
set -e

echo "Starting Commerce OS development environment..."

make infra-light
echo "Waiting for infrastructure..."
sleep 15

make create-dbs
make install-shared

echo ""
echo "Infrastructure ready!"
echo "Now start services in IntelliJ or run:"
echo "  mvn spring-boot:run -pl platform/auth-service"
echo "  mvn spring-boot:run -pl commerce/order-service"
echo ""
