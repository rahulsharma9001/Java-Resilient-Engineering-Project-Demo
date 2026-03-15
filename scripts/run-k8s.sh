#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
MAVEN_REPO_LOCAL="$ROOT_DIR/.m2/repository"
MAVEN_ARGS="-Dmaven.repo.local=$MAVEN_REPO_LOCAL"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1"
    exit 1
  fi
}

require_cmd kubectl
require_cmd docker

echo "Building order-service jar..."
(cd "$ROOT_DIR/resilience" && ./mvnw clean package -DskipTests $MAVEN_ARGS)

echo "Building payment-service jar..."
(cd "$ROOT_DIR/services/payment-service" && ./mvnw clean package -DskipTests $MAVEN_ARGS)

echo "Building Docker images..."
docker build -t resilience-order-service:local "$ROOT_DIR/resilience"
docker build -t resilience-payment-service:local "$ROOT_DIR/services/payment-service"

if kubectl config current-context | grep -q '^kind-'; then
  if ! command -v kind >/dev/null 2>&1; then
    echo "Detected kind context but 'kind' command is missing."
    echo "Install kind or load images into cluster nodes manually."
    exit 1
  fi
  CLUSTER_NAME="$(kubectl config current-context | sed 's/^kind-//')"
  echo "Detected kind cluster: $CLUSTER_NAME. Loading images into kind..."
  kind load docker-image resilience-order-service:local --name "$CLUSTER_NAME"
  kind load docker-image resilience-payment-service:local --name "$CLUSTER_NAME"
elif command -v minikube >/dev/null 2>&1 && kubectl config current-context | grep -qi minikube; then
  echo "Detected minikube context. Loading images into minikube..."
  minikube image load resilience-order-service:local
  minikube image load resilience-payment-service:local
else
  echo "Using current cluster context without explicit image-load step."
  echo "Ensure nodes can pull/use images: resilience-order-service:local and resilience-payment-service:local"
fi

echo "Applying Kubernetes manifests..."
kubectl apply -k "$ROOT_DIR/infra/k8s"

echo "Waiting for deployments..."
kubectl rollout status deploy/payment-service -n resilience-demo --timeout=180s
kubectl rollout status deploy/order-service -n resilience-demo --timeout=180s

echo "Kubernetes app stack is ready in namespace: resilience-demo"
echo "Tip for local testing:" 
echo "  kubectl port-forward -n resilience-demo svc/order-service 8080:8080"
echo "  kubectl port-forward -n resilience-demo svc/payment-service 8081:8081"
