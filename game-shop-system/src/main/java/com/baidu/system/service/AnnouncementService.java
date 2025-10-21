package com.baidu.system.service;

import com.baidu.common.ApiResponse;
import com.baidu.system.entity.Announcement;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {
    
    /**
     * 创建公告
     */
    ApiResponse<Announcement> createAnnouncement(String title, String content, Long createdBy, 
                                               LocalDateTime publishTime, LocalDateTime expireTime, Integer priority);
    
    /**
     * 根据ID查询公告
     */
    ApiResponse<Announcement> getAnnouncementById(Long id);
    
    /**
     * 更新公告
     */
    ApiResponse<Announcement> updateAnnouncement(Long id, String title, String content, 
                                               LocalDateTime publishTime, LocalDateTime expireTime, Integer priority);
    
    /**
     * 删除公告
     */
    ApiResponse<Void> deleteAnnouncement(Long id);
    
    /**
     * 分页查询公告列表（公开）
     */
    ApiResponse<Page<Announcement>> listPublicAnnouncements(int page, int size);
    
    /**
     * 分页查询所有公告（管理员）
     */
    ApiResponse<Page<Announcement>> listAllAnnouncements(int page, int size);
    
    /**
     * 发布公告
     */
    ApiResponse<Void> publishAnnouncement(Long id);
    
    /**
     * 下线公告
     */
    ApiResponse<Void> unpublishAnnouncement(Long id);
    
    /**
     * 更新公告状态
     */
    ApiResponse<Void> updateAnnouncementStatus(Long id, String status);
    
    /**
     * 增加浏览次数
     */
    ApiResponse<Void> incrementViewCount(Long id);
    
    /**
     * 查询置顶公告
     */
    ApiResponse<List<Announcement>> getTopAnnouncements(int limit);
}
