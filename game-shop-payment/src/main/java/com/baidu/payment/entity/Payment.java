package com.baidu.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("payments")
@Getter
@Setter
public class Payment {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("user_id")
    private Long userId;

    private BigDecimal amount;

    @TableField("payment_method")
    private String paymentMethod;

    private PaymentStatus status = PaymentStatus.PENDING;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("third_party_id")
    private String thirdPartyId;

    @TableField("payment_time")
    private LocalDateTime paymentTime;

    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @TableField("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    private String remark;
}
