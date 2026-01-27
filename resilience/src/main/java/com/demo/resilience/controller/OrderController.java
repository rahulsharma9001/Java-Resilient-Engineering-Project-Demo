package com.demo.resilience.controller;

import com.demo.resilience.model.OrderRequest;
import com.demo.resilience.model.OrderResponse;
import com.demo.resilience.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/place")
    public CompletableFuture<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
        return orderService.placeOrder(request);
    }
}
