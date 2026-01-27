package com.demo.resilience.client;

import com.demo.resilience.model.OrderRequest;
import com.demo.resilience.model.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "payment-service",
        url = "http://localhost:8080",
        fallback = com.demo.resilience.fallback.PaymentFallback.class
)
public interface PaymentClient {

    @PostMapping("/payment/pay")
    PaymentResponse makePayment(OrderRequest request);
}
