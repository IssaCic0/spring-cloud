package com.baidu.api.payment;

import com.baidu.common.ApiResponse;
import com.baidu.api.payment.dto.PaymentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "game-shop-payment", path = "/api/payments")
public interface PaymentFeignClient {
    
    @GetMapping("/{paymentId}")
    ApiResponse<PaymentDTO> getPaymentById(@PathVariable("paymentId") Long paymentId);
    
    @PostMapping("/create")
    ApiResponse<PaymentDTO> createPayment(@RequestBody CreatePaymentRequest request);
}

