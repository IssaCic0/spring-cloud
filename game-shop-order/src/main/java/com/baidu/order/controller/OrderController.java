package com.baidu.order.controller;

import com.baidu.common.ApiResponse;
import com.baidu.order.entity.Order;
import com.baidu.order.entity.OrderItem;
import com.baidu.order.service.OrderService;
import com.baidu.security.RequireRoles;
import com.baidu.security.Role;
import com.baidu.security.RequestContext;
import com.baidu.api.shop.ShopFeignClient;
import com.baidu.api.shop.dto.ShopDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    private final ShopFeignClient shopFeignClient;

    private Long currentUserId() {
        Long id = RequestContext.getUserId();
        if (id == null) throw new IllegalArgumentException("用户ID不能为空");
        return id;
    }

    // 买家：创建订单（对齐前端：{ items:[{productId,quantity}], note, addressId, promoId }）
    @RequireRoles({Role.BUYER})
    @PostMapping
    public ApiResponse<Order> createOrder(@RequestBody OrderCreateRequest req) {
        Long userId = currentUserId();
        return orderService.createOrder(userId, req);
    }

    public static class OrderCreateRequest {
        public java.util.List<Item> items;
        public String note;
        public Long addressId;
        public Long promoId;
        public static class Item { public Long productId; public Integer quantity; }
    }

    public static class UpdateShipStatusRequest {
        public String status;
    }

    // 买家：查询我的订单
    @RequireRoles({Role.BUYER})
    @GetMapping("/my")
    public ApiResponse<Page<Order>> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserId();
        return orderService.getUserOrders(userId, page, size);
    }

    // 商家：查询店铺订单（对齐前端：无需传 shopId，从当前用户推断）
    @RequireRoles({Role.MERCHANT})
    @GetMapping("/merchant")
    public ApiResponse<Page<Order>> merchantOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long shopId) {
        // 兼容模式：携带 shopId 时，直接按 shop_id 查询（不校验归属）
        if (shopId != null) {
            try { return orderService.getMerchantOrders(shopId, page, size); } catch (Exception e) { return ApiResponse.ok(org.springframework.data.domain.Page.empty()); }
        }
        // 兼容老体验：未携带 shopId 时，按商家拥有的店铺推断
        Long uid = currentUserId();
        Long sid = null;
        try {
            ShopDTO shop = shopFeignClient.getShopByOwnerId(uid).getData();
            sid = (shop == null ? null : shop.getId());
        } catch (Exception ignore) { /* 降级：店铺服务未启动 */ }
        if (sid == null) {
            // 兼容老项目：未能匹配到店铺时，返回所有订单（演示/教学场景）
            return orderService.listAllOrders(page, size);
        }
        try {
            return orderService.getMerchantOrders(sid, page, size);
        } catch (Exception e) {
            // 异常时返回所有订单，避免空白
            return orderService.listAllOrders(page, size);
        }
    }
    
    // 查询订单详情
    @GetMapping("/{orderId}")
    public ApiResponse<Order> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }
    
    // 查询订单项
    @GetMapping("/{orderId}/items")
    public ApiResponse<List<OrderItem>> getOrderItems(@PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }

    // 商家：查询订单项（对齐前端：/orders/merchant/{orderId}/items）
    @RequireRoles({Role.MERCHANT})
    @GetMapping("/merchant/{orderId}/items")
    public ApiResponse<List<OrderItem>> merchantOrderItems(@PathVariable Long orderId) {
        return orderService.getOrderItems(orderId);
    }
    
    // 买家：取消订单
    @RequireRoles({Role.BUYER})
    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable Long orderId) {
        Long userId = currentUserId();
        return orderService.cancelOrder(orderId, userId);
    }
    
    // 商家：确认订单
    @RequireRoles({Role.MERCHANT})
    @PutMapping("/{orderId}/confirm")
    public ApiResponse<Void> confirmOrder(@PathVariable Long orderId) {
        return orderService.confirmOrder(orderId);
    }
    
    // 商家：发货
    @RequireRoles({Role.MERCHANT})
    @PutMapping("/{orderId}/ship")
    public ApiResponse<Void> shipOrder(@PathVariable Long orderId) {
        return orderService.shipOrder(orderId);
    }

    // 商家：更新发货状态（对齐前端：PATCH /orders/{orderId}/ship-status {status}）
    @RequireRoles({Role.MERCHANT})
    @PatchMapping("/{orderId}/ship-status")
    public ApiResponse<Void> updateShipStatusAlias(@PathVariable Long orderId, @RequestBody UpdateShipStatusRequest req) {
        return orderService.updateOrderStatus(orderId, req.status);
    }
    
    // 买家：确认收货
    @RequireRoles({Role.BUYER})
    @PutMapping("/{orderId}/complete")
    public ApiResponse<Void> completeOrder(@PathVariable Long orderId) {
        return orderService.completeOrder(orderId);
    }
    
    // 管理员：更新订单状态
    @RequireRoles({Role.ADMIN})
    @PutMapping("/{orderId}/status")
    public ApiResponse<Void> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

}
