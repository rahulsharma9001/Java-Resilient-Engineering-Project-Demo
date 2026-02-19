package com.demo.payment.controller;

import com.demo.payment.model.FailureModeRequest;
import com.demo.payment.model.PaymentRequest;
import com.demo.payment.model.PaymentResponse;
import com.demo.payment.service.PaymentFailureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFailureService paymentFailureService;

    @PostMapping("/pay")
    public PaymentResponse pay(@RequestBody PaymentRequest request) {
        paymentFailureService.applyBehavior();
        return new PaymentResponse("APPROVED", "Payment approved for order " + request.getOrderId());
    }

    @PostMapping("/mode")
    public FailureModeRequest setMode(@RequestBody FailureModeRequest request) {
        paymentFailureService.update(request);
        return paymentFailureService.current();
    }

    @GetMapping("/mode")
    public FailureModeRequest getMode() {
        return paymentFailureService.current();
    }
}
