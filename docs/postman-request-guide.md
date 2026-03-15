# Postman Request Guide (Step-by-Step Demo)

This guide documents every Postman request used in the demo, in exact execution order.

## Base setup
- Order Service base URL: `http://localhost:8080`
- Payment Service base URL: `http://localhost:8081`
- Content type for POST requests: `application/json`

### Payment mode admin credentials
Used for `POST /payment/mode` and `GET /payment/mode`:
- Username: `payment-admin`
- Password: `change-this-password`

If overridden via env vars, use:
- `PAYMENT_ADMIN_USERNAME`
- `PAYMENT_ADMIN_PASSWORD`

## Request sequence

## 01 - Place Order (Normal Flow)
- Purpose: Show baseline success path.
- Method: `POST`
- URL: `http://localhost:8080/order/place`
- Auth: `No Auth`
- Headers:
  - `Content-Type: application/json`
- Body:
```json
{
  "orderId": "ORD-1001",
  "amount": "100"
}
```
- Expected response:
  - HTTP `200`
  - `status` normally `APPROVED`
- What to explain: Payment dependency is healthy, order is approved.

## 02 - Set Payment Mode: ALWAYS_FAIL
- Purpose: Force dependency failure.
- Method: `POST`
- URL: `http://localhost:8081/payment/mode`
- Auth: `Basic Auth` (`payment-admin` / `change-this-password`)
- Headers:
  - `Content-Type: application/json`
- Body:
```json
{
  "mode": "ALWAYS_FAIL"
}
```
- Expected response:
  - HTTP `200`
  - Returned mode object confirms `ALWAYS_FAIL`
- What to explain: Payment service is now configured to fail every call.

## 03 - Place Order (Fallback Demo)
- Purpose: Show graceful degradation when dependency is down.
- Method: `POST`
- URL: `http://localhost:8080/order/place`
- Auth: `No Auth`
- Headers:
  - `Content-Type: application/json`
- Body:
```json
{
  "orderId": "ORD-1002",
  "amount": "200"
}
```
- Expected response:
  - HTTP `200`
  - `status: "PENDING"`
  - Fallback message (degraded mode)
- What to explain: Circuit breaker/retry/fallback protect user-facing API.

## 04 - Set Payment Mode: DELAY (3000ms)
- Purpose: Simulate slow downstream service.
- Method: `POST`
- URL: `http://localhost:8081/payment/mode`
- Auth: `Basic Auth`
- Headers:
  - `Content-Type: application/json`
- Body:
```json
{
  "mode": "DELAY",
  "delayMs": 3000
}
```
- Expected response:
  - HTTP `200`
  - Returned mode object confirms `DELAY` and `delayMs: 3000`
- What to explain: Dependency is not down, but too slow.

## 05 - Place Order (Timeout Fallback)
- Purpose: Show timeout handling through TimeLimiter + fallback.
- Method: `POST`
- URL: `http://localhost:8080/order/place`
- Auth: `No Auth`
- Headers:
  - `Content-Type: application/json`
- Body:
```json
{
  "orderId": "ORD-1003",
  "amount": "300"
}
```
- Expected response:
  - HTTP `200`
  - Usually `status: "PENDING"` due to timeout fallback
- What to explain: Slow dependency is treated as failure to protect user latency.

## 06 - Set Payment Mode: RANDOM_FAIL (60%)
- Purpose: Simulate intermittent/random failures.
- Method: `POST`
- URL: `http://localhost:8081/payment/mode`
- Auth: `Basic Auth`
- Headers:
  - `Content-Type: application/json`
- Body:
```json
{
  "mode": "RANDOM_FAIL",
  "failurePercent": 60
}
```
- Expected response:
  - HTTP `200`
  - Returned mode object confirms `RANDOM_FAIL` and `failurePercent: 60`
- What to explain: Each payment call now has ~60% chance to fail.

## 07 - Place Order (Retry/Intermittent)
- Purpose: Show retry behavior under unstable dependency.
- Method: `POST`
- URL: `http://localhost:8080/order/place`
- Auth: `No Auth`
- Headers:
  - `Content-Type: application/json`
- Body:
```json
{
  "orderId": "ORD-1004",
  "amount": "400"
}
```
- Expected response:
  - HTTP `200`
  - Can be either:
    - `APPROVED` (if a retry attempt eventually succeeds), or
    - `PENDING` (if all retry attempts fail or breaker is open)
- What to explain: Intermittent failures are mitigated by retries; fallback handles remaining failures.

## 08 - Check Current Payment Mode
- Purpose: Verify current injected mode during demo.
- Method: `GET`
- URL: `http://localhost:8081/payment/mode`
- Auth: `Basic Auth`
- Headers: none mandatory
- Body: none
- Expected response:
  - HTTP `200`
  - JSON showing current `mode`, `delayMs`, `failurePercent`
- What to explain: You can prove test setup before each scenario.

## 09 - Reset Payment Mode: NORMAL
- Purpose: Return system to healthy behavior after demo.
- Method: `POST`
- URL: `http://localhost:8081/payment/mode`
- Auth: `Basic Auth`
- Headers:
  - `Content-Type: application/json`
- Body:
```json
{
  "mode": "NORMAL",
  "delayMs": 0,
  "failurePercent": 0
}
```
- Expected response:
  - HTTP `200`
  - Returned mode object confirms `NORMAL`
- What to explain: Cleanup step to end demo in a stable state.

## Common response patterns to mention in session
- `APPROVED`: dependency call succeeded.
- `PENDING`: fallback path was used (failure, timeout, breaker-open, or retries exhausted).
- HTTP `200` with `PENDING` is expected in resilience demos because graceful degradation is intentional.

## Postman folder suggestion
Create one folder and keep this exact order:
1. `01 - Place Order (Normal Flow)`
2. `02 - Set Payment Mode: ALWAYS_FAIL`
3. `03 - Place Order (Fallback Demo)`
4. `04 - Set Payment Mode: DELAY (3000ms)`
5. `05 - Place Order (Timeout Fallback)`
6. `06 - Set Payment Mode: RANDOM_FAIL (60%)`
7. `07 - Place Order (Retry/Intermittent)`
8. `08 - Check Current Payment Mode`
9. `09 - Reset Payment Mode: NORMAL`

## How to present in session (using these requests)
1. Run `01` and say: "Healthy dependency gives APPROVED."
2. Run `02` then `03` and say: "Failure is isolated; user gets controlled PENDING."
3. Run `04` then `05` and say: "Timeout path protects user latency."
4. Run `06` then run `07` multiple times and say: "Intermittent failures are mitigated by retry + fallback."
5. Run `08` to prove active mode configuration during discussion.
6. Run `09` at the end and say: "System restored to normal."

## Quick troubleshooting
- If `02/04/06/08/09` return `401`, re-check Basic Auth credentials.
- If `07` keeps returning `PENDING`, run multiple times and wait 10-15 seconds for breaker recovery window.
- If services are unreachable, restart stack with `./scripts/run-local.sh`.
