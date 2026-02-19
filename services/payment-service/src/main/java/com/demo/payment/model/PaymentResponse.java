package com.demo.payment.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String paymentStatus;
    private String message;
}
