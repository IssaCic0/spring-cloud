package com.baidu.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baidu.product.entity.Category;
import com.baidu.product.mapper.CategoryMapper;
import com.baidu.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public org.springframework.data.domain.Page<Category> list(int page, int size, Boolean enabled) {
        QueryWrapper<Category> qw = new QueryWrapper<>();
        if (enabled != null) qw.eq("enabled", enabled);
        qw.orderByAsc("sort").orderByDesc("created_at");
        Page<Category> mp = categoryMapper.selectPage(new Page<>(page + 1L, size), qw);
        return new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal());
    }

    @Override
    public org.springframework.data.domain.Page<Category> adminList(int page, int size, String keyword, Boolean enabled) {
        QueryWrapper<Category> qw = new QueryWrapper<>();
        if (enabled != null) qw.eq("enabled", enabled);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("name", keyword).or().like("slug", keyword));
        }
        qw.orderByAsc("sort").orderByDesc("created_at");
        Page<Category> mp = categoryMapper.selectPage(new Page<>(page + 1L, size), qw);
        return new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal());
    }

    @Override
    @Transactional
    public Category adminCreate(String name, String slug, Long parentId, Integer sort, Boolean enabled) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setParentId(parentId);
        c.setSort(sort);
        c.setEnabled(enabled == null ? Boolean.TRUE : enabled);
        categoryMapper.insert(c);
        return c;
    }

    @Override
    public Category adminGet(Long id) {
        Category c = categoryMapper.selectById(id);
        if (c == null) throw new NoSuchElementException("分类不存在");
        return c;
    }

    @Override
    @Transactional
    public void adminUpdate(Long id, String name, String slug, Long parentId, Integer sort, Boolean enabled) {
        Category c = categoryMapper.selectById(id);
        if (c == null) throw new NoSuchElementException("分类不存在");
        if (name != null) c.setName(name);
        if (slug != null) c.setSlug(slug);
        if (parentId != null) c.setParentId(parentId);
        if (sort != null) c.setSort(sort);
        if (enabled != null) c.setEnabled(enabled);
        categoryMapper.updateById(c);
    }

    @Override
    @Transactional
    public void adminDelete(Long id) {
        categoryMapper.deleteById(id);
    }
}
