package com.baidu.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@TableName("shops")
@Getter
@Setter
public class Shop {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("owner_id")
    private Long ownerId;

    private String name;

    private String description;

    @TableField("logo_url")
    private String logoUrl;

    private ShopStatus status = ShopStatus.PENDING_APPROVAL;

    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @TableField("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}

