# Failure Scenarios

1. **Dependency Down**
- Trigger: payment mode `ALWAYS_FAIL`
- Patterns validated: `CircuitBreaker`, `Retry`, `Fallback`

2. **High Latency Dependency**
- Trigger: payment mode `DELAY`
- Patterns validated: `TimeLimiter`, `Fallback`, graceful degradation

3. **Intermittent Errors**
- Trigger: payment mode `RANDOM_FAIL`
- Patterns validated: `Retry` before fallback

4. **Load Pressure**
- Trigger: load script with random failures
- Patterns validated: `Bulkhead`, `RateLimiter`

5. **Pod/Network Infrastructure Faults (K8s)**
- Trigger: Litmus manifests in `chaos/litmus`
- Patterns validated: system behavior under infra-level disruption
