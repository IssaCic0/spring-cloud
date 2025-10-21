package com.baidu.api.payment;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String method;
}

