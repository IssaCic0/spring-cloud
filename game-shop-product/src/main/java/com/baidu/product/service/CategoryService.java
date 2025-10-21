package com.baidu.product.service;

import com.baidu.product.entity.Category;
import org.springframework.data.domain.Page;

public interface CategoryService {
    Page<Category> list(int page, int size, Boolean enabled);
    Page<Category> adminList(int page, int size, String keyword, Boolean enabled);
    Category adminCreate(String name, String slug, Long parentId, Integer sort, Boolean enabled);
    Category adminGet(Long id);
    void adminUpdate(Long id, String name, String slug, Long parentId, Integer sort, Boolean enabled);
    void adminDelete(Long id);
}
