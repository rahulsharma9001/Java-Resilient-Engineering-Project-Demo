package com.demo.payment.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotBlank(message = "orderId is required")
    private String orderId;
    @NotBlank(message = "amount is required")
    private String amount;
}
