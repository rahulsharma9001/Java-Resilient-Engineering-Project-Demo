package com.demo.payment.service;

import com.demo.payment.model.FailureMode;
import com.demo.payment.model.FailureModeRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PaymentFailureService {

    private final AtomicReference<FailureMode> mode = new AtomicReference<>(FailureMode.NORMAL);
    private final AtomicInteger delayMs = new AtomicInteger(0);
    private final AtomicInteger failurePercent = new AtomicInteger(0);

    public void applyBehavior() {
        FailureMode currentMode = mode.get();
        if (currentMode == FailureMode.ALWAYS_FAIL) {
            throw new RuntimeException("Injected failure: ALWAYS_FAIL mode");
        }

        if (currentMode == FailureMode.DELAY) {
            sleep(delayMs.get());
        }

        if (currentMode == FailureMode.RANDOM_FAIL) {
            int roll = ThreadLocalRandom.current().nextInt(100);
            if (roll < failurePercent.get()) {
                throw new RuntimeException("Injected failure: RANDOM_FAIL mode");
            }
        }
    }

    public synchronized void update(FailureModeRequest request) {
        FailureMode requestMode = request.getMode() == null ? FailureMode.NORMAL : request.getMode();
        mode.set(requestMode);
        delayMs.set(Math.max(0, request.getDelayMs()));
        int percent = request.getFailurePercent();
        failurePercent.set(Math.max(0, Math.min(100, percent)));
    }

    public FailureModeRequest current() {
        FailureModeRequest response = new FailureModeRequest();
        response.setMode(mode.get());
        response.setDelayMs(delayMs.get());
        response.setFailurePercent(failurePercent.get());
        return response;
    }

    private void sleep(int ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Delay interrupted", ex);
        }
    }
}
