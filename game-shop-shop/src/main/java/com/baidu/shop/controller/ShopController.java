package com.baidu.shop.controller;

import com.baidu.common.ApiResponse;
import com.baidu.shop.entity.Shop;
import com.baidu.shop.entity.ShopStatus;
import com.baidu.security.RequestContext;
import com.baidu.security.RequireRoles;
import com.baidu.security.Role;
import com.baidu.shop.service.ShopService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/shops")
@Validated
public class ShopController {

    @Autowired
    private ShopService shopService;

    private Long currentUserId() {
        Long id = RequestContext.getUserId();
        if (id == null) throw new IllegalArgumentException("用户ID不能为空");
        return id;
    }

    // 商家：开店
    @RequireRoles({Role.MERCHANT})
    @PostMapping("/me")
    public ApiResponse<Map<String, Object>> openShop(@RequestBody @Validated ShopUpsertRequest req) {
        Shop s = shopService.openShop(currentUserId(), req.name, req.description, req.logoUrl);
        Map<String, Object> m = new HashMap<>();
        m.put("shopId", s.getId());
        return ApiResponse.ok(m);
    }

    // 商家关店
    @RequireRoles({Role.MERCHANT})
    @DeleteMapping("/me")
    public ApiResponse<Void> closeShop() {
        shopService.closeShop(currentUserId());
        return ApiResponse.ok();
    }

    // 商家：查看我的店铺
    @RequireRoles({Role.MERCHANT})
    @GetMapping("/me")
    public ApiResponse<Shop> getMyShop() {
        Shop shop = shopService.getMyShop(currentUserId());
        return ApiResponse.ok(shop);
    }

    // 商家：我的店铺列表（对齐前端：/api/shops/my）
    @RequireRoles({Role.MERCHANT})
    @GetMapping("/my")
    public ApiResponse<Map<String, Object>> listMyShops(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Page<Shop> p = shopService.listMyShops(currentUserId(), page, size);
        Map<String, Object> r = new HashMap<>();
        r.put("page", p.getNumber());
        r.put("size", p.getSize());
        r.put("total", p.getTotalElements());
        r.put("items", p.getContent());
        return ApiResponse.ok(r);
    }

    // 商家：创建店铺（对齐前端：POST /api/shops）
    @RequireRoles({Role.MERCHANT})
    @PostMapping
    public ApiResponse<Map<String, Object>> createMyShop(@RequestBody @Validated ShopUpsertRequest req) {
        Shop s = shopService.createShop(currentUserId(), req.name, req.description, req.logoUrl);
        Map<String, Object> m = new HashMap<>();
        m.put("shopId", s.getId());
        return ApiResponse.ok(m);
    }

    // 商家：按ID获取（对齐前端：GET /api/shops/my/{shopId}）
    @RequireRoles({Role.MERCHANT})
    @GetMapping("/my/{shopId}")
    public ApiResponse<Map<String, Object>> getMyShopById(@PathVariable Long shopId) {
        Shop s = shopService.getMyShop(currentUserId(), shopId);
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getId());
        m.put("name", s.getName());
        m.put("description", s.getDescription());
        m.put("logoUrl", s.getLogoUrl());
        m.put("status", s.getStatus());
        return ApiResponse.ok(m);
    }

    // 商家：更新（对齐前端：PUT /api/shops/{shopId}）
    @RequireRoles({Role.MERCHANT})
    @PutMapping("/{shopId}")
    public ApiResponse<Void> updateMyShop(@PathVariable Long shopId, @RequestBody @Validated ShopUpsertRequest req) {
        shopService.updateMyShop(currentUserId(), shopId, req.name, req.description, req.logoUrl);
        return ApiResponse.ok();
    }

    // 商家：关闭（对齐前端：DELETE /api/shops/{shopId}）
    @RequireRoles({Role.MERCHANT})
    @DeleteMapping("/{shopId}")
    public ApiResponse<Void> closeMyShop(@PathVariable Long shopId) {
        shopService.closeMyShop(currentUserId(), shopId);
        return ApiResponse.ok();
    }

    // 商家：物理删除（对齐前端：DELETE /api/shops/{shopId}/hard）
    @RequireRoles({Role.MERCHANT})
    @DeleteMapping("/{shopId}/hard")
    public ApiResponse<Void> deleteMyShopHardById(@PathVariable Long shopId) {
        shopService.deleteMyShop(currentUserId(), shopId);
        return ApiResponse.ok();
    }

    // 商家：更新店铺信息
    @RequireRoles({Role.MERCHANT})
    @PutMapping("/me")
    public ApiResponse<Void> updateMyShop(@RequestBody @Validated ShopUpsertRequest req) {
        shopService.updateShop(currentUserId(), req.name, req.description, req.logoUrl);
        return ApiResponse.ok();
    }

    // 管理员：查询所有店铺
    @RequireRoles({Role.ADMIN})
    @GetMapping("/admin")
    public ApiResponse<Map<String, Object>> adminListShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String name) {
        Page<Shop> p = shopService.adminList(page, size, ownerId, name);
        Map<String, Object> resp = new HashMap<>();
        resp.put("page", p.getNumber());
        resp.put("size", p.getSize());
        resp.put("total", p.getTotalElements());
        resp.put("items", p.getContent());
        return ApiResponse.ok(resp);
    }

    // 管理员：审批店铺
    @RequireRoles({Role.ADMIN})
    @PostMapping("/admin/{shopId}/approve")
    public ApiResponse<Void> approveShop(@PathVariable Long shopId) {
        shopService.approve(shopId);
        return ApiResponse.ok();
    }

    // 管理员：封禁店铺
    @RequireRoles({Role.ADMIN})
    @PostMapping("/admin/{shopId}/ban")
    public ApiResponse<Void> banShop(@PathVariable Long shopId) {
        shopService.ban(shopId);
        return ApiResponse.ok();
    }

    // Feign接口：根据ID获取店铺
    @GetMapping("/{shopId}")
    public ApiResponse<Shop> getShopById(@PathVariable Long shopId) {
        Shop shop = shopService.adminGet(shopId);
        return ApiResponse.ok(shop);
    }

    // Feign接口：根据店主ID获取店铺
    @GetMapping("/owner/{ownerId}")
    public ApiResponse<Shop> getShopByOwnerId(@PathVariable Long ownerId) {
        Shop shop = shopService.getMyShop(ownerId);
        return ApiResponse.ok(shop);
    }

    public static class ShopUpsertRequest {
        @NotBlank
        public String name;
        public String description;
        public String logoUrl;
    }
}
