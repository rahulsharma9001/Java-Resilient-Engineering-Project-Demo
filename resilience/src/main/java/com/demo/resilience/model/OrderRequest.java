package com.demo.resilience.model;

import lombok.Data;

@Data
public class OrderRequest {
    private String orderId;
    private String amount;
}
