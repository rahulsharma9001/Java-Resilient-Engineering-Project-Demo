# Resilience Engineering Demo Flow

## 1. Start stack
```bash
./scripts/run-local.sh
```

## 2. Happy path
```bash
curl -X POST http://localhost:8080/order/place \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-100","amount":"100"}'
```
Expected: `APPROVED`

## 3. Inject controlled app failure
```bash
curl -X POST http://localhost:8081/payment/mode \
  -u payment-admin:change-this-password \
  -H 'Content-Type: application/json' \
  -d '{"mode":"ALWAYS_FAIL"}'
```
Call order endpoint again. Expected: `PENDING` fallback response.

## 4. Inject latency
```bash
curl -X POST http://localhost:8081/payment/mode \
  -u payment-admin:change-this-password \
  -H 'Content-Type: application/json' \
  -d '{"mode":"DELAY","delayMs":3000}'
```
Expected: `TimeLimiter` fallback starts triggering.

## 5. Random failures + retries
```bash
curl -X POST http://localhost:8081/payment/mode \
  -u payment-admin:change-this-password \
  -H 'Content-Type: application/json' \
  -d '{"mode":"RANDOM_FAIL","failurePercent":60}'
./scripts/run-load-test.sh 40
```

## 6. Show observability
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (`admin`/`admin`)
- Order-service metrics: `http://localhost:8080/actuator/prometheus`

## 7. Kubernetes chaos (optional)
```bash
./scripts/run-chaos.sh pod-delete
./scripts/run-chaos.sh network-latency
./scripts/run-chaos.sh cpu-hog
```
