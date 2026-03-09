# Resilience Engineering Demo

This repository provides an end-to-end resilience demo with:
- `order-service` protected by Resilience4j
- `payment-service` with switchable failure modes
- Prometheus + Grafana observability
- Litmus chaos experiment templates

## Project Layout
- `resilience/`: order-service (Spring Boot + Feign + Resilience4j)
- `services/payment-service/`: downstream dependency for injected failures
- `infra/`: docker-compose + Prometheus + Grafana provisioning
- `chaos/litmus/`: Kubernetes chaos manifests
- `scripts/`: run and demo scripts
- `docs/`: session demo flow and failure scenarios

## Quick Start
```bash
./scripts/run-local.sh
```

## Demo Docs
- Postman request-by-request guide: `docs/postman-request-guide.md`

## Useful Endpoints
- `POST /order/place`: `http://localhost:8080/order/place`
- `POST /payment/mode` (requires basic auth): `http://localhost:8081/payment/mode`
- `GET /payment/mode` (requires basic auth): `http://localhost:8081/payment/mode`
- `Order metrics`: `http://localhost:8080/actuator/prometheus`
- `Payment metrics`: `http://localhost:8081/actuator/prometheus`

Default mode-admin credentials:
- Username: `payment-admin`
- Password: `change-this-password`
