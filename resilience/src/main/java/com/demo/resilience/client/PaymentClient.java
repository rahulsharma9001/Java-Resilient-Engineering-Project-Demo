package com.demo.resilience.client;

import com.demo.resilience.model.OrderRequest;
import com.demo.resilience.model.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "payment-service",
        url = "${payment.service.url:http://localhost:8081}"
)
public interface PaymentClient {

    @PostMapping("/payment/pay")
    PaymentResponse makePayment(@RequestBody OrderRequest request);
}
