# Pre-Demo Checklist (Run + Verify)

Use this checklist before presenting the demo.

## 1. Go to project root
```bash
cd /home/nashtech/Resilience-Engineering
```

## 2. Verify code builds and tests
```bash
cd services/payment-service && ./mvnw -q test -Dmaven.repo.local=../../.m2/repository
cd ../../resilience && ./mvnw -q test -Dmaven.repo.local=../.m2/repository
cd ..
```
Expected:
- Both test commands pass.

## 3. Verify Docker is healthy (mandatory)
```bash
docker context ls
docker info
docker ps
```
Expected:
- `docker info` works without `500` or socket errors.

If failing:
- Restart Docker Desktop.
- Re-run the 3 commands above.

## 4. Local demo path (Docker Compose)
```bash
./scripts/run-local.sh
```
Expected:
- order-service: `http://localhost:8080`
- payment-service: `http://localhost:8081`
- prometheus: `http://localhost:9090`
- grafana: `http://localhost:3000`

## 5. Kubernetes + Litmus path
```bash
./scripts/run-k8s.sh
./scripts/setup-litmus.sh
./scripts/run-chaos.sh pod-delete
kubectl get chaosengine,chaosresult -n resilience-demo
```
Expected:
- app deployments ready in `resilience-demo`
- chaos resources created
- chaos result generated

## 6. If local ports are occupied
Use alternate local ports:
```bash
kubectl port-forward -n resilience-demo svc/order-service 18080:8080
kubectl port-forward -n resilience-demo svc/payment-service 18081:8081
```

Use these endpoints then:
- Order API: `http://localhost:18080/order/place`
- Payment Mode API: `http://localhost:18081/payment/mode`

## 7. Quick API smoke check
```bash
curl -s -u payment-admin:change-this-password http://localhost:18081/payment/mode
curl -s -X POST http://localhost:18080/order/place -H 'Content-Type: application/json' -d '{"orderId":"ORD-1","amount":"100"}'
```
Expected:
- mode endpoint returns JSON
- order endpoint returns `APPROVED` or controlled fallback, not crash

## 8. Post-demo cleanup
```bash
(cd infra && docker compose down)
kubectl delete chaosengine --all -n resilience-demo
kubectl delete chaosresult --all -n resilience-demo
kubectl delete -k infra/k8s
```
