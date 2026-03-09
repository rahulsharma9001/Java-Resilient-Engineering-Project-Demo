package com.demo.payment.service;

import com.demo.payment.model.FailureMode;
import com.demo.payment.model.FailureModeRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentFailureServiceTest {

    private final PaymentFailureService service = new PaymentFailureService();

    @Test
    void alwaysFailModeThrowsException() {
        FailureModeRequest request = new FailureModeRequest();
        request.setMode(FailureMode.ALWAYS_FAIL);
        service.update(request);

        assertThrows(RuntimeException.class, service::applyBehavior);
    }

    @Test
    void updateClampsOutOfRangeValues() {
        FailureModeRequest request = new FailureModeRequest();
        request.setMode(FailureMode.RANDOM_FAIL);
        request.setDelayMs(-10);
        request.setFailurePercent(200);
        service.update(request);

        FailureModeRequest current = service.current();
        assertEquals(FailureMode.RANDOM_FAIL, current.getMode());
        assertEquals(0, current.getDelayMs());
        assertEquals(100, current.getFailurePercent());
    }

    @Test
    void normalModeDoesNotThrow() {
        FailureModeRequest request = new FailureModeRequest();
        request.setMode(FailureMode.NORMAL);
        service.update(request);

        assertDoesNotThrow(service::applyBehavior);
    }
}
