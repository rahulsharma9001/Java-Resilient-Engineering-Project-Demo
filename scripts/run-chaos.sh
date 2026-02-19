#!/usr/bin/env bash
set -euo pipefail

EXPERIMENT="${1:-pod-delete}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
MANIFEST="$ROOT_DIR/chaos/litmus/${EXPERIMENT}.yaml"

if [[ ! -f "$MANIFEST" ]]; then
  echo "Unknown experiment: $EXPERIMENT"
  echo "Available: pod-delete, network-latency, cpu-hog"
  exit 1
fi

echo "Applying Litmus experiment: $EXPERIMENT"
kubectl apply -f "$MANIFEST"
