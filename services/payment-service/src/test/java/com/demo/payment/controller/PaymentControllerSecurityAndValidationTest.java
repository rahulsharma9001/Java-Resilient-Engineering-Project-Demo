package com.demo.payment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerSecurityAndValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void modeEndpointRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/payment/mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"NORMAL\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void modeEndpointAllowsAuthenticatedRequest() throws Exception {
        mockMvc.perform(post("/payment/mode")
                        .with(httpBasic("payment-admin", "change-this-password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"NORMAL\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void payEndpointValidatesInputBody() throws Exception {
        mockMvc.perform(post("/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"\",\"amount\":\"100\"}"))
                .andExpect(status().isBadRequest());
    }
}
