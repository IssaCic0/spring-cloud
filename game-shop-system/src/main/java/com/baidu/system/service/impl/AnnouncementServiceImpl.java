package com.baidu.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baidu.common.ApiResponse;
import com.baidu.system.entity.Announcement;
import com.baidu.system.entity.AnnouncementStatus;
import com.baidu.system.mapper.AnnouncementMapper;
import com.baidu.system.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    @Transactional
    public ApiResponse<Announcement> createAnnouncement(String title, String content, Long createdBy, LocalDateTime publishTime, LocalDateTime expireTime, Integer priority) {
        Announcement a = new Announcement();
        a.setTitle(title);
        a.setContent(content);
        a.setCreatedBy(createdBy);
        a.setPublishTime(publishTime);
        a.setExpireTime(expireTime);
        a.setPriority(priority == null ? 0 : priority);
        a.setStatus(AnnouncementStatus.ACTIVE);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        announcementMapper.insert(a);
        return ApiResponse.ok(a);
    }

    @Override
    public ApiResponse<Announcement> getAnnouncementById(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new java.util.NoSuchElementException("公告不存在");
        return ApiResponse.ok(a);
    }

    @Override
    @Transactional
    public ApiResponse<Announcement> updateAnnouncement(Long id, String title, String content, LocalDateTime publishTime, LocalDateTime expireTime, Integer priority) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new java.util.NoSuchElementException("公告不存在");
        if (title != null) a.setTitle(title);
        if (content != null) a.setContent(content);
        if (publishTime != null) a.setPublishTime(publishTime);
        if (expireTime != null) a.setExpireTime(expireTime);
        if (priority != null) a.setPriority(priority);
        a.setUpdatedAt(LocalDateTime.now());
        announcementMapper.updateById(a);
        return ApiResponse.ok(a);
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @Override
    public ApiResponse<org.springframework.data.domain.Page<Announcement>> listPublicAnnouncements(int page, int size) {
        // 兼容老表：不依赖 status/publish_time/expire_time，仅按创建时间倒序
        Page<Announcement> mp = announcementMapper.selectPage(new Page<>(page + 1L, size),
                new QueryWrapper<Announcement>().orderByDesc("created_at"));
        return ApiResponse.ok(new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal()));
    }

    @Override
    public ApiResponse<org.springframework.data.domain.Page<Announcement>> listAllAnnouncements(int page, int size) {
        Page<Announcement> mp = announcementMapper.selectPage(new Page<>(page + 1L, size),
                new QueryWrapper<Announcement>().orderByDesc("created_at"));
        return ApiResponse.ok(new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal()));
    }

    @Override
    @Transactional
    public ApiResponse<Void> publishAnnouncement(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new java.util.NoSuchElementException("公告不存在");
        a.setStatus(AnnouncementStatus.ACTIVE);
        a.setPublishTime(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        announcementMapper.updateById(a);
        return ApiResponse.ok();
    }

    @Override
    @Transactional
    public ApiResponse<Void> unpublishAnnouncement(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new java.util.NoSuchElementException("公告不存在");
        a.setStatus(AnnouncementStatus.INACTIVE);
        a.setUpdatedAt(LocalDateTime.now());
        announcementMapper.updateById(a);
        return ApiResponse.ok();
    }

    @Override
    @Transactional
    public ApiResponse<Void> updateAnnouncementStatus(Long id, String status) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new java.util.NoSuchElementException("公告不存在");
        try { a.setStatus(AnnouncementStatus.valueOf(status)); } catch (IllegalArgumentException e) { throw new IllegalArgumentException("非法状态"); }
        a.setUpdatedAt(LocalDateTime.now());
        announcementMapper.updateById(a);
        return ApiResponse.ok();
    }

    @Override
    @Transactional
    public ApiResponse<Void> incrementViewCount(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) return ApiResponse.ok();
        a.setViewCount(a.getViewCount() == null ? 1 : a.getViewCount() + 1);
        announcementMapper.updateById(a);
        return ApiResponse.ok();
    }

    @Override
    public ApiResponse<List<Announcement>> getTopAnnouncements(int limit) {
        Page<Announcement> mp = announcementMapper.selectPage(new Page<>(1, limit),
                new QueryWrapper<Announcement>().orderByDesc("created_at"));
        return ApiResponse.ok(mp.getRecords());
    }
}
