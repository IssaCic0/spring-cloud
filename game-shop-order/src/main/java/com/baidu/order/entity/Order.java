package com.baidu.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("orders")
@Getter
@Setter
public class Order {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 数据库为 buyer_id
    @TableField("buyer_id")
    private Long userId;

    @TableField("shop_id")
    private Long shopId;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    private OrderStatus status = OrderStatus.PENDING;

    // 兼容老库：无以下列，避免 SQL 选择
    @TableField(exist = false)
    private String paymentMethod;

    @TableField(exist = false)
    private String paymentStatus;

    @TableField(exist = false)
    private String shippingAddress;

    // 备注列名为 note
    @TableField("note")
    private String orderNote;

    // 发货状态列 ship_status
    @TableField("ship_status")
    private String shipStatus;

    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @TableField("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
