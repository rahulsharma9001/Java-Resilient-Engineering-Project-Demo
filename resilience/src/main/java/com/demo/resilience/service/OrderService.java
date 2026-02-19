package com.demo.resilience.service;

import com.demo.resilience.client.PaymentClient;
import com.demo.resilience.model.OrderRequest;
import com.demo.resilience.model.OrderResponse;
import com.demo.resilience.model.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final PaymentClient paymentClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public OrderResponse placeOrder(OrderRequest request) {

        CircuitBreaker circuitBreaker =
                circuitBreakerFactory.create("paymentService");

        return circuitBreaker.run(
                () -> {
                    PaymentResponse payment =
                            paymentClient.makePayment(request);

                    return new OrderResponse(
                            request.getOrderId(),
                            payment.getPaymentStatus(),
                            payment.getMessage()
                    );
                },
                throwable -> paymentFallback(request, throwable)
        );
    }

    private OrderResponse paymentFallback(
            OrderRequest request,
            Throwable ex) {

        return new OrderResponse(
                request.getOrderId(),
                "PENDING",
                "Order placed, payment will be processed later"
        );
    }
}
