package com.demo.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class PaymentErrorHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> onError(Exception ex) {
        return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()));
    }
}
