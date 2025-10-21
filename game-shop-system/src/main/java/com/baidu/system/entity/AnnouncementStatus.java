package com.baidu.system.entity;

/**
 * 公告状态枚举
 */
public enum AnnouncementStatus {
    DRAFT("草稿"),
    ACTIVE("已发布"),
    INACTIVE("已下线"),
    EXPIRED("已过期");

    private final String description;

    AnnouncementStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
