package com.demo.resilience.service;

import com.demo.resilience.client.PaymentClient;
import com.demo.resilience.model.OrderRequest;
import com.demo.resilience.model.OrderResponse;
import com.demo.resilience.model.PaymentResponse;
import lombok.RequiredArgsConstructor;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final PaymentClient paymentClient;
    private final Executor resilienceExecutor;

    public OrderResponse placeOrder(OrderRequest request) {
        try {
            PaymentResponse payment = processPaymentWithTimeLimiter(request).join();
            return new OrderResponse(request.getOrderId(), payment.getPaymentStatus(), payment.getMessage());
        } catch (CompletionException ex) {
            return toOrderFallback(request, ex.getCause() == null ? ex : ex.getCause());
        } catch (Exception ex) {
            return toOrderFallback(request, ex);
        }
    }

    @TimeLimiter(name = "paymentService", fallbackMethod = "timeLimiterFallback")
    public CompletableFuture<PaymentResponse> processPaymentWithTimeLimiter(OrderRequest request) {
        return CompletableFuture.supplyAsync(() -> processPaymentProtected(request), resilienceExecutor);
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService", fallbackMethod = "paymentFallback")
    @RateLimiter(name = "paymentService", fallbackMethod = "paymentFallback")
    @Bulkhead(name = "paymentService", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "paymentFallback")
    public PaymentResponse processPaymentProtected(OrderRequest request) {
        return paymentClient.makePayment(request);
    }

    public CompletableFuture<PaymentResponse> timeLimiterFallback(OrderRequest request, Throwable ex) {
        return CompletableFuture.completedFuture(paymentFallback(request, ex));
    }

    public PaymentResponse paymentFallback(OrderRequest request, Throwable ex) {
        return new PaymentResponse(
                "PENDING",
                "Order accepted in degraded mode. Payment will be processed asynchronously."
        );
    }

    private OrderResponse toOrderFallback(OrderRequest request, Throwable ex) {
        PaymentResponse fallback = paymentFallback(request, ex);
        return new OrderResponse(request.getOrderId(), fallback.getPaymentStatus(), fallback.getMessage());
    }
}
