package com.demo.resilience.controller;

import com.demo.resilience.model.PaymentResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class FakePaymentController {
    @PostMapping("/pay")
    public PaymentResponse pay(){
        throw  new RuntimeException("Payment Service down");
    }
}
