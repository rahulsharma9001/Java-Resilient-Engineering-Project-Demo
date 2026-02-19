package com.demo.payment.model;

import lombok.Data;

@Data
public class FailureModeRequest {
    private FailureMode mode = FailureMode.NORMAL;
    private int delayMs = 0;
    private int failurePercent = 0;
}
