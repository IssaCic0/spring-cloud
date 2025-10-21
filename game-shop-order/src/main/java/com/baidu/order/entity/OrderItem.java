package com.baidu.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("order_items")
@Getter
@Setter
public class OrderItem {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("product_id")
    private Long productId;

    @TableField("title_snapshot")
    private String productTitle;

    @TableField("price_snapshot")
    private BigDecimal productPrice;

    private Integer quantity;

    @TableField("subtotal")
    private BigDecimal subtotal;

    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
