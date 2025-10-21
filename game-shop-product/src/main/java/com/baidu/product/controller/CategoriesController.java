package com.baidu.product.controller;

import com.baidu.common.ApiResponse;
import com.baidu.product.entity.Category;
import com.baidu.product.service.CategoryService;
import com.baidu.security.RequireRoles;
import com.baidu.security.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@Validated
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoryService categoryService;

    // 公共列表
    @GetMapping
    public ApiResponse<Map<String, Object>> list(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam(required = false) Boolean enabled) {
        Page<Category> p = categoryService.list(page, size, enabled);
        Map<String, Object> r = new HashMap<>();
        r.put("page", p.getNumber());
        r.put("size", p.getSize());
        r.put("total", p.getTotalElements());
        r.put("items", p.getContent());
        return ApiResponse.ok(r);
    }

    // 管理列表
    @RequireRoles({Role.ADMIN})
    @GetMapping("/admin")
    public ApiResponse<Map<String, Object>> adminList(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) Boolean enabled) {
        Page<Category> p = categoryService.adminList(page, size, keyword, enabled);
        Map<String, Object> r = new HashMap<>();
        r.put("page", p.getNumber());
        r.put("size", p.getSize());
        r.put("total", p.getTotalElements());
        r.put("items", p.getContent());
        return ApiResponse.ok(r);
    }

    // 管理新增
    @RequireRoles({Role.ADMIN})
    @PostMapping("/admin")
    public ApiResponse<Map<String, Object>> adminCreate(@RequestBody AdminUpsertRequest req) {
        Category c = categoryService.adminCreate(req.name, req.slug, req.parentId, req.sort, req.enabled);
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        return ApiResponse.ok(m);
    }

    // 管理详情
    @RequireRoles({Role.ADMIN})
    @GetMapping("/admin/{id}")
    public ApiResponse<Map<String, Object>> adminGet(@PathVariable Long id) {
        Category c = categoryService.adminGet(id);
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("slug", c.getSlug());
        m.put("parentId", c.getParentId());
        m.put("sort", c.getSort());
        m.put("enabled", c.getEnabled());
        m.put("createdAt", c.getCreatedAt());
        m.put("updatedAt", c.getUpdatedAt());
        return ApiResponse.ok(m);
    }

    // 管理更新
    @RequireRoles({Role.ADMIN})
    @PutMapping("/admin/{id}")
    public ApiResponse<Void> adminUpdate(@PathVariable Long id, @RequestBody AdminUpsertRequest req) {
        categoryService.adminUpdate(id, req.name, req.slug, req.parentId, req.sort, req.enabled);
        return ApiResponse.ok();
    }

    // 管理删除
    @RequireRoles({Role.ADMIN})
    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> adminDelete(@PathVariable Long id) {
        categoryService.adminDelete(id);
        return ApiResponse.ok();
    }

    public static class AdminUpsertRequest {
        @NotBlank public String name;
        public String slug;
        public Long parentId;
        public Integer sort;
        public Boolean enabled;
    }
}
