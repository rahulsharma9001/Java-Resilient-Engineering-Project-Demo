# Rollback Plan

1. Set payment mode back to normal:
```bash
curl -X POST http://localhost:8081/payment/mode \
  -u payment-admin:change-this-password \
  -H 'Content-Type: application/json' \
  -d '{"mode":"NORMAL","delayMs":0,"failurePercent":0}'
```

2. Remove Litmus experiment resources:
```bash
kubectl delete chaosengine --all -n default
```

3. Stop local stack:
```bash
(cd infra && docker compose down)
```
