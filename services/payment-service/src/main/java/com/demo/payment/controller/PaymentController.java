package com.demo.payment.controller;

import com.demo.payment.model.FailureModeRequest;
import com.demo.payment.model.PaymentRequest;
import com.demo.payment.model.PaymentResponse;
import com.demo.payment.service.PaymentFailureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFailureService paymentFailureService;
    private final Executor paymentExecutor;

    @PostMapping("/pay")
    public CompletableFuture<PaymentResponse> pay(@Valid @RequestBody PaymentRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            paymentFailureService.applyBehavior();
            return new PaymentResponse("APPROVED", "Payment approved for order " + request.getOrderId());
        }, paymentExecutor);
    }

    @PostMapping("/mode")
    public FailureModeRequest setMode(@Valid @RequestBody FailureModeRequest request) {
        paymentFailureService.update(request);
        return paymentFailureService.current();
    }

    @GetMapping("/mode")
    public FailureModeRequest getMode() {
        return paymentFailureService.current();
    }
}
