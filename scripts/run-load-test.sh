#!/usr/bin/env bash
set -euo pipefail

REQ_COUNT="${1:-30}"

echo "Sending ${REQ_COUNT} requests to order-service..."
for i in $(seq 1 "$REQ_COUNT"); do
  payload="{\"orderId\":\"ORD-$i\",\"amount\":\"100\"}"
  curl -s -X POST "http://localhost:8080/order/place" \
    -H "Content-Type: application/json" \
    -d "$payload"
  echo
  sleep 0.2
done
