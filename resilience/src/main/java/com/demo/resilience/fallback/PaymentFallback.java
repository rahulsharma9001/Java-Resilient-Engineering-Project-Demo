package com.demo.resilience.fallback;

import com.demo.resilience.client.PaymentClient;
import com.demo.resilience.model.OrderRequest;
import com.demo.resilience.model.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentFallback implements PaymentClient {

    @Override
    public PaymentResponse makePayment(OrderRequest request) {
        return new PaymentResponse(
                "PENDING",
                "Payment Service is currently unavailable, please try again later"
        );
    }
}
