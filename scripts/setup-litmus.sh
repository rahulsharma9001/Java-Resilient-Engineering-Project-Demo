#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
NAMESPACE="resilience-demo"
CHAOS_CHART_VERSION="${CHAOS_CHART_VERSION:-3.26.0}"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1"
    exit 1
  fi
}

require_cmd kubectl
require_cmd curl
require_cmd tar

echo "Checking Litmus CRDs..."
if ! kubectl get crd chaosengines.litmuschaos.io >/dev/null 2>&1; then
  cat <<MSG
Litmus CRDs are not installed on this cluster.
Install Litmus first, then rerun this script.
Reference: https://docs.litmuschaos.io/
MSG
  exit 1
fi

echo "Applying namespace and Litmus RBAC for app namespace: $NAMESPACE"
kubectl apply -f "$ROOT_DIR/infra/k8s/namespace.yaml"
kubectl apply -f "$ROOT_DIR/infra/k8s/litmus-rbac.yaml"

echo "Downloading chaos-charts release: $CHAOS_CHART_VERSION"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT
CHART_ARCHIVE_URL="https://github.com/litmuschaos/chaos-charts/archive/${CHAOS_CHART_VERSION}.tar.gz"
if ! curl -fsSL "$CHART_ARCHIVE_URL" -o "$TMP_DIR/charts.tgz"; then
  echo "Release archive not found for version $CHAOS_CHART_VERSION, falling back to master branch archive."
  CHART_ARCHIVE_URL="https://github.com/litmuschaos/chaos-charts/archive/refs/heads/master.tar.gz"
  curl -fsSL "$CHART_ARCHIVE_URL" -o "$TMP_DIR/charts.tgz"
fi

tar -xzf "$TMP_DIR/charts.tgz" -C "$TMP_DIR"

CHART_DIR="$(find "$TMP_DIR" -maxdepth 1 -type d -name 'chaos-charts-*' | head -n 1 || true)"
if [[ -z "$CHART_DIR" ]]; then
  echo "Failed to locate extracted chaos-charts directory."
  exit 1
fi

apply_fault() {
  local fault_name="$1"
  local fault_file
  fault_file="$(find "$CHART_DIR" -name fault.yaml | grep "kubernetes/${fault_name}" | head -n 1 || true)"
  if [[ -z "$fault_file" ]]; then
    echo "Unable to locate fault manifest for: $fault_name"
    exit 1
  fi
  echo "Applying ChaosExperiment for fault: $fault_name"
  kubectl apply -n "$NAMESPACE" -f "$fault_file"
}

echo "Applying ChaosExperiment resources (if not already present)..."
apply_fault "pod-delete"
apply_fault "pod-network-latency"
apply_fault "pod-cpu-hog"

echo "Litmus setup complete for namespace: $NAMESPACE"
