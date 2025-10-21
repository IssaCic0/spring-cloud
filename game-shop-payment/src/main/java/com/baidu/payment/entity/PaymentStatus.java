package com.baidu.payment.entity;

/**
 * 支付状态枚举
 */
public enum PaymentStatus {
    PENDING("待支付"),
    PROCESSING("支付中"),
    SUCCESS("支付成功"),
    FAILED("支付失败"),
    CANCELLED("已取消"),
    REFUNDED("已退款"),
    PARTIAL_REFUNDED("部分退款");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
