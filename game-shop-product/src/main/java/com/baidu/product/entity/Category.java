package com.baidu.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@TableName("categories")
@Getter
@Setter
public class Category {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String slug;

    @TableField("parent_id")
    private Long parentId;

    private Integer sort;

    private Boolean enabled = true;

    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @TableField("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
