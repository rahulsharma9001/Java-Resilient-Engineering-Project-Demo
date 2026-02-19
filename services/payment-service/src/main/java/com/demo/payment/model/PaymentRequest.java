package com.demo.payment.model;

import lombok.Data;

@Data
public class PaymentRequest {
    private String orderId;
    private String amount;
}
