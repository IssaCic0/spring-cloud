package com.baidu.system.controller;

import com.baidu.common.ApiResponse;
import com.baidu.security.RequireRoles;
import com.baidu.security.Role;
import com.baidu.security.RequestContext;
import com.baidu.system.entity.Announcement;
import com.baidu.system.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementsController {
    
    private final AnnouncementService announcementService;
    
    private Long currentUserId() {
        Long id = RequestContext.getUserId();
        if (id == null) throw new IllegalArgumentException("用户ID不能为空");
        return id;
    }

    // 公开：查询公告列表
    @GetMapping
    public ApiResponse<Page<Announcement>> listAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return announcementService.listPublicAnnouncements(page, size);
    }
    
    // 公开：查询公告详情
    @GetMapping("/{id}")
    public ApiResponse<Announcement> getAnnouncementById(@PathVariable Long id) {
        // 增加浏览次数
        announcementService.incrementViewCount(id);
        return announcementService.getAnnouncementById(id);
    }
    
    // 公开：查询置顶公告
    @GetMapping("/top")
    public ApiResponse<List<Announcement>> getTopAnnouncements(@RequestParam(defaultValue = "5") int limit) {
        return announcementService.getTopAnnouncements(limit);
    }

    // 管理员：创建公告（原路径）
    @RequireRoles({Role.ADMIN})
    @PostMapping
    public ApiResponse<Announcement> createAnnouncement(@RequestBody AnnouncementCreateRequest req) {
        Long createdBy = currentUserId();
        return announcementService.createAnnouncement(req.title, req.content, createdBy,
                                                    req.publishTime, req.expireTime, req.priority);
    }

    // 管理员：创建公告（对齐前端：/admin）
    @RequireRoles({Role.ADMIN})
    @PostMapping("/admin")
    public ApiResponse<Announcement> adminCreate(@RequestBody AnnouncementCreateRequest req) {
        Long createdBy = currentUserId();
        return announcementService.createAnnouncement(req.title, req.content, createdBy,
                                                    req.publishTime, req.expireTime, req.priority);
    }
    
    // 管理员：详情（对齐前端：GET /api/announcements/admin/{id}）
    @RequireRoles({Role.ADMIN})
    @GetMapping("/admin/{id}")
    public ApiResponse<Announcement> adminGet(@PathVariable Long id) {
        return announcementService.getAnnouncementById(id);
    }

    // 管理员：更新公告（原路径）
    @RequireRoles({Role.ADMIN})
    @PutMapping("/{id}")
    public ApiResponse<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody AnnouncementCreateRequest req) {
        return announcementService.updateAnnouncement(id, req.title, req.content,
                                                    req.publishTime, req.expireTime, req.priority);
    }

    // 管理员：更新公告（对齐前端：/admin/{id}）
    @RequireRoles({Role.ADMIN})
    @PutMapping("/admin/{id}")
    public ApiResponse<Announcement> adminUpdate(@PathVariable Long id, @RequestBody AnnouncementCreateRequest req) {
        return announcementService.updateAnnouncement(id, req.title, req.content,
                                                    req.publishTime, req.expireTime, req.priority);
    }
    
    // 管理员：删除公告（原路径）
    @RequireRoles({Role.ADMIN})
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAnnouncement(@PathVariable Long id) {
        return announcementService.deleteAnnouncement(id);
    }

    // 管理员：删除公告（对齐前端：/admin/{id}）
    @RequireRoles({Role.ADMIN})
    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> adminDelete(@PathVariable Long id) {
        return announcementService.deleteAnnouncement(id);
    }
    
    // 管理员：查询所有公告（原路径）
    @RequireRoles({Role.ADMIN})
    @GetMapping("/admin/all")
    public ApiResponse<Page<Announcement>> listAllAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return announcementService.listAllAnnouncements(page, size);
    }

    // 管理员：查询所有公告（对齐前端：/admin）
    @RequireRoles({Role.ADMIN})
    @GetMapping("/admin")
    public ApiResponse<Map<String,Object>> adminList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Announcement> p = announcementService.listAllAnnouncements(page, size).getData();
        Map<String,Object> r = new HashMap<>();
        if(p!=null){
            r.put("page", p.getNumber());
            r.put("size", p.getSize());
            r.put("total", p.getTotalElements());
            r.put("items", p.getContent());
        } else {
            r.put("page", page);
            r.put("size", size);
            r.put("total", 0);
            r.put("items", java.util.Collections.emptyList());
        }
        return ApiResponse.ok(r);
    }
    
    // 管理员：发布公告
    @RequireRoles({Role.ADMIN})
    @PutMapping("/{id}/publish")
    public ApiResponse<Void> publishAnnouncement(@PathVariable Long id) {
        return announcementService.publishAnnouncement(id);
    }

    // 管理员：发布公告（对齐前端：/admin/{id} 依旧保持扩展路径不冲突）
    
    // 管理员：下线公告
    @RequireRoles({Role.ADMIN})
    @PutMapping("/{id}/unpublish")
    public ApiResponse<Void> unpublishAnnouncement(@PathVariable Long id) {
        return announcementService.unpublishAnnouncement(id);
    }
    
    // 管理员：更新公告状态
    @RequireRoles({Role.ADMIN})
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateAnnouncementStatus(@PathVariable Long id, @RequestParam String status) {
        return announcementService.updateAnnouncementStatus(id, status);
    }

    public static class AnnouncementCreateRequest {
        public String title;
        public String content;
        public LocalDateTime publishTime;
        public LocalDateTime expireTime;
        public Integer priority;
    }
}
