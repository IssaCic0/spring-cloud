package com.baidu.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@TableName("announcements")
@Getter
@Setter
public class Announcement {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    // 下列字段在现有数据库中不存在，为兼容老表，标记为 exist=false，避免 SQL 选择这些列
    @TableField(exist = false)
    private AnnouncementStatus status;

    @TableField(exist = false)
    private Long createdBy;

    @TableField("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @TableField("updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @TableField(exist = false)
    private LocalDateTime publishTime;

    @TableField(exist = false)
    private LocalDateTime expireTime;

    @TableField(exist = false)
    private Integer priority;

    @TableField(exist = false)
    private Integer viewCount;
}
