# Session Showcase Runbook (End-to-End)

This runbook gives you a complete script to run and present every component of the project during your session.

## 1. Pre-checks (before session)

Ensure these are installed:
- Java 17 (or higher)
- Docker + Docker Compose
- `curl`
- Kubernetes + `kubectl` (only if you want to run chaos experiments)
- Litmus installed in cluster (only for chaos part)

Quick checks:
```bash
java -version
docker --version
docker compose version
kubectl version --client
```

## 2. Project map (what you are going to show)
- `resilience/`: Order Service (resilience logic)
- `services/payment-service/`: Payment Service (injectable failure modes)
- `infra/`: Docker Compose + Prometheus + Grafana
- `chaos/litmus/`: chaos experiments
- `scripts/`: ready-to-run helper scripts
- `docs/project-overall-flow-explanation.md`: non-technical flow + graph

## 3. Start full stack
From repo root:
```bash
./scripts/run-local.sh
```

This builds and starts:
- Order Service: `http://localhost:8080`
- Payment Service: `http://localhost:8081`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000` (`admin/admin`)

Payment mode admin credentials (can be overridden with env vars `PAYMENT_ADMIN_USERNAME` and `PAYMENT_ADMIN_PASSWORD`):
- Default username: `payment-admin`
- Default password: `change-this-password`

## 4. Component health check

### 4.1 Check payment mode
```bash
curl -s -u payment-admin:change-this-password http://localhost:8081/payment/mode
```
Expected: mode is `NORMAL`.

### 4.2 Check actuator metrics endpoints
```bash
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8081/actuator/health
curl -s http://localhost:8080/actuator/prometheus | head
curl -s http://localhost:8081/actuator/prometheus | head
```

## 5. Demo flow to showcase all components

## 5.1 Baseline (normal business flow)
Show that system works normally.

```bash
curl -s -X POST http://localhost:8080/order/place \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-1001","amount":"100"}'
```
Expected: response with status `APPROVED`.

What to explain:
- User calls Order Service.
- Order Service calls Payment Service.
- Payment succeeds.

## 5.2 Controlled failure: payment always fails
Show graceful degradation (fallback).

```bash
curl -s -X POST http://localhost:8081/payment/mode \
  -u payment-admin:change-this-password \
  -H 'Content-Type: application/json' \
  -d '{"mode":"ALWAYS_FAIL"}'

curl -s -X POST http://localhost:8080/order/place \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-1002","amount":"200"}'
```
Expected: Order Service returns `PENDING` fallback instead of crashing.

What to explain:
- Circuit breaker/retry/fallback protect user experience.
- Dependency failed, but main API is still available.

## 5.3 Controlled latency: payment delay
Show timeout handling.

```bash
curl -s -X POST http://localhost:8081/payment/mode \
  -u payment-admin:change-this-password \
  -H 'Content-Type: application/json' \
  -d '{"mode":"DELAY","delayMs":3000}'

curl -s -X POST http://localhost:8080/order/place \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-1003","amount":"300"}'
```
Expected: timeout path triggers fallback (`PENDING`).

What to explain:
- Slow systems are also failures.
- Time limiter prevents user from waiting too long.

## 5.4 Intermittent issues: random failures
Show retry behavior under unstable dependency.

```bash
curl -s -X POST http://localhost:8081/payment/mode \
  -u payment-admin:change-this-password \
  -H 'Content-Type: application/json' \
  -d '{"mode":"RANDOM_FAIL","failurePercent":60}'

./scripts/run-load-test.sh 40
```
Expected:
- Mix of `APPROVED` and `PENDING` depending on retries and fallback.

What to explain:
- Retries recover transient failures.
- Remaining failures are handled gracefully via fallback.

## 5.5 Load + protection behavior
(Optional but recommended)
```bash
./scripts/run-load-test.sh 100
```
What to explain:
- Bulkhead limits concurrent pressure.
- Rate limiter protects against request spikes.

## 6. Observability showcase (Prometheus + Grafana)

## 6.1 Prometheus
Open `http://localhost:9090`.

Try queries such as:
- `http_server_requests_seconds_count`
- `resilience4j_circuitbreaker_state`
- `resilience4j_circuitbreaker_calls`
- `resilience4j_retry_calls`
- `resilience4j_ratelimiter_calls`

## 6.2 Grafana
Open `http://localhost:3000` and login `admin/admin`.

Show:
- Live request behavior during normal flow
- Error/fallback increase during failure modes
- Recovery trend when service returns to normal

## 7. Chaos testing (Kubernetes optional segment)
Run only if Kubernetes + Litmus are ready.

```bash
./scripts/run-chaos.sh pod-delete
./scripts/run-chaos.sh network-latency
./scripts/run-chaos.sh cpu-hog
```

What to explain:
- These are infrastructure-level disruptions.
- Resilience patterns should still keep user-facing API stable.

## 8. Recovery and rollback
Set payment back to normal:
```bash
curl -s -X POST http://localhost:8081/payment/mode \
  -u payment-admin:change-this-password \
  -H 'Content-Type: application/json' \
  -d '{"mode":"NORMAL","delayMs":0,"failurePercent":0}'
```

(Optional) remove chaos resources:
```bash
kubectl delete chaosengine --all -n default
```

Stop local stack:
```bash
(cd infra && docker compose down)
```

## 9. Suggested speaking sequence (time-boxed)
- 0-3 min: explain architecture from `docs/project-overall-flow-explanation.md`
- 3-6 min: show normal flow (`APPROVED`)
- 6-10 min: show fail mode (`PENDING` fallback)
- 10-13 min: show delay mode (timeout fallback)
- 13-16 min: show random failures + load script
- 16-20 min: show Prometheus/Grafana evidence
- Optional extension: Kubernetes chaos experiments

## 10. Quick command bundle (copy/paste)
```bash
./scripts/run-local.sh

curl -s -X POST http://localhost:8080/order/place -H 'Content-Type: application/json' -d '{"orderId":"ORD-1001","amount":"100"}'

curl -s -X POST http://localhost:8081/payment/mode -u payment-admin:change-this-password -H 'Content-Type: application/json' -d '{"mode":"ALWAYS_FAIL"}'
curl -s -X POST http://localhost:8080/order/place -H 'Content-Type: application/json' -d '{"orderId":"ORD-1002","amount":"200"}'

curl -s -X POST http://localhost:8081/payment/mode -u payment-admin:change-this-password -H 'Content-Type: application/json' -d '{"mode":"DELAY","delayMs":3000}'
curl -s -X POST http://localhost:8080/order/place -H 'Content-Type: application/json' -d '{"orderId":"ORD-1003","amount":"300"}'

curl -s -X POST http://localhost:8081/payment/mode -u payment-admin:change-this-password -H 'Content-Type: application/json' -d '{"mode":"RANDOM_FAIL","failurePercent":60}'
./scripts/run-load-test.sh 40

curl -s -X POST http://localhost:8081/payment/mode -u payment-admin:change-this-password -H 'Content-Type: application/json' -d '{"mode":"NORMAL","delayMs":0,"failurePercent":0}'
```
