package com.demo.payment.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FailureModeRequest {
    private FailureMode mode = FailureMode.NORMAL;
    @Min(value = 0, message = "delayMs must be >= 0")
    @Max(value = 30000, message = "delayMs must be <= 30000")
    private int delayMs = 0;
    @Min(value = 0, message = "failurePercent must be >= 0")
    @Max(value = 100, message = "failurePercent must be <= 100")
    private int failurePercent = 0;
}
