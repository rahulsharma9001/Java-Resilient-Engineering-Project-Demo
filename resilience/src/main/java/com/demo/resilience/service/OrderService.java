package com.demo.resilience.service;

import com.demo.resilience.client.PaymentClient;
import com.demo.resilience.model.OrderRequest;
import com.demo.resilience.model.OrderResponse;
import com.demo.resilience.model.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final PaymentClient paymentClient;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    @TimeLimiter(name = "paymentService")
    public CompletableFuture<OrderResponse> placeOrder(OrderRequest request) {

        return CompletableFuture.supplyAsync(() -> {

            PaymentResponse payment =
                    paymentClient.makePayment(request);

            return new OrderResponse(
                    request.getOrderId(),
                    payment.getPaymentStatus(),
                    payment.getMessage()
            );
        });
    }

    public CompletableFuture<OrderResponse> paymentFallback(
            OrderRequest request,
            Throwable ex) {

        return CompletableFuture.completedFuture(
                new OrderResponse(
                        request.getOrderId(),
                        "PENDING",
                        "Order placed, payment will be processed later"
                )
        );
    }
}
