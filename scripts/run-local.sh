#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
MAVEN_REPO_LOCAL="$ROOT_DIR/.m2/repository"
MAVEN_ARGS="-Dmaven.repo.local=$MAVEN_REPO_LOCAL"

echo "Building order-service..."
(cd "$ROOT_DIR/resilience" && ./mvnw clean package -DskipTests $MAVEN_ARGS)

echo "Building payment-service..."
(cd "$ROOT_DIR/services/payment-service" && ./mvnw clean package -DskipTests $MAVEN_ARGS)

echo "Starting docker compose stack..."
(cd "$ROOT_DIR/infra" && docker compose up --build -d)

echo "Stack started:"
echo "- order-service: http://localhost:8080"
echo "- payment-service: http://localhost:8081"
echo "- prometheus: http://localhost:9090"
echo "- grafana: http://localhost:3000 (admin/admin)"
