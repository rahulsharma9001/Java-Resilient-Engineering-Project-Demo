# LitmusChaos End-to-End (Kubernetes)

This guide completes the Litmus implementation path in this repository.

## What is now included in repo
- Kubernetes manifests for app deployment:
  - `infra/k8s/namespace.yaml`
  - `infra/k8s/payment-service.yaml`
  - `infra/k8s/order-service.yaml`
  - `infra/k8s/litmus-rbac.yaml`
  - `infra/k8s/kustomization.yaml`
- ChaosEngine manifests targeting Kubernetes deployment:
  - `chaos/litmus/pod-delete.yaml`
  - `chaos/litmus/network-latency.yaml`
  - `chaos/litmus/cpu-hog.yaml`
- Scripts:
  - `scripts/run-k8s.sh` (build images + deploy apps to K8s)
  - `scripts/setup-litmus.sh` (validate Litmus CRDs + apply RBAC + experiment CRs)
  - `scripts/run-chaos.sh` (apply selected chaos engine)

## Prerequisites
- Java 17+
- Docker
- Kubernetes cluster (`kind` or `minikube` recommended)
- `kubectl`
- Litmus installed in cluster (CRDs/operator)

## 1. Deploy app stack to Kubernetes
From repository root:

```bash
./scripts/run-k8s.sh
```

This will:
- Build both jars
- Build Docker images
- Load images to `kind`/`minikube` when detected
- Deploy `order-service` and `payment-service` in namespace `resilience-demo`

## 2. Verify Litmus prerequisites and setup app-namespace resources

```bash
./scripts/setup-litmus.sh
```

This will:
- Check that Litmus CRDs exist
- Apply `litmus-admin` service account and RBAC
- Apply ChaosExperiment resources for pod-delete/network-latency/cpu-hog

If CRD check fails, install Litmus first and rerun.

## 3. Port-forward for local demo calls
Use separate terminals:

```bash
kubectl port-forward -n resilience-demo svc/order-service 8080:8080
```

```bash
kubectl port-forward -n resilience-demo svc/payment-service 8081:8081
```

## 4. Run baseline and resilience scenarios
- Use Postman requests from `docs/postman-request-guide.md`
- Confirm normal and fallback behavior before chaos injection

## 5. Trigger Litmus chaos experiments

```bash
./scripts/run-chaos.sh pod-delete
./scripts/run-chaos.sh network-latency
./scripts/run-chaos.sh cpu-hog
```

All target namespace: `resilience-demo` by default.

You can override namespace:

```bash
./scripts/run-chaos.sh pod-delete resilience-demo
```

## 6. Observe chaos results

```bash
kubectl get chaosengine -n resilience-demo
kubectl get chaosresult -n resilience-demo
kubectl describe chaosengine payment-pod-delete -n resilience-demo
kubectl logs -n resilience-demo -l name=pod-delete -f
```

## 7. Cleanup

```bash
kubectl delete chaosengine --all -n resilience-demo
kubectl delete chaosresult --all -n resilience-demo
kubectl delete -k infra/k8s
```

## Demo talking points
- App-level resilience: Resilience4j still returns controlled fallback (`PENDING`)
- Infra-level failure injection: Litmus kills pods / injects latency / CPU stress
- User-facing API remains available with graceful degradation

## How to present the Litmus segment correctly
1. Transition line: "So far we injected app-level faults; now we inject infrastructure faults."
2. Show readiness:
   - `kubectl get pods -n resilience-demo`
   - Confirm `order-service` and `payment-service` are running.
3. Trigger one experiment at a time:
   - `./scripts/run-chaos.sh pod-delete`
   - Keep sending order requests from Postman request `07`.
4. Show evidence:
   - `kubectl get chaosresult -n resilience-demo`
   - API still responds (`APPROVED`/`PENDING`), not system crash.
5. Repeat for `network-latency` and `cpu-hog` briefly.
6. Cleanup and close:
   - `kubectl delete chaosengine --all -n resilience-demo`
   - `kubectl delete chaosresult --all -n resilience-demo`
