package com.baidu.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baidu.api.product.ProductFeignClient;
import com.baidu.api.product.dto.ProductDTO;
import com.baidu.common.ApiResponse;
import com.baidu.order.entity.Order;
import com.baidu.order.entity.OrderItem;
import com.baidu.order.entity.OrderStatus;
import com.baidu.order.mapper.OrderItemMapper;
import com.baidu.order.mapper.OrderMapper;
import com.baidu.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductFeignClient productFeignClient;

    @Override
    @Transactional
    public ApiResponse<Order> createOrder(Long userId, com.baidu.order.controller.OrderController.OrderCreateRequest req) {
        if (req == null || req.items == null || req.items.isEmpty()) {
            throw new IllegalArgumentException("订单项不能为空");
        }
        // 获取第一件商品信息用于确定店铺ID等
        var first = req.items.get(0);
        ProductDTO firstProd = productFeignClient.getProductById(first.productId).getData();
        if (firstProd == null) throw new IllegalArgumentException("商品不存在");
        Long shopId = firstProd.getShopId();

        Order order = new Order();
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setOrderNote(req.note);
        order.setShippingAddress(null); // addressId 未对接，此处留空或扩展地址服务
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        orderMapper.insert(order);

        List<OrderItem> savedItems = new ArrayList<>();
        for (var it : req.items) {
            ProductDTO p = productFeignClient.getProductById(it.productId).getData();
            if (p == null) throw new IllegalArgumentException("商品不存在: " + it.productId);
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(p.getId());
            oi.setProductTitle(p.getTitle());
            oi.setProductPrice(p.getPrice());
            oi.setQuantity(it.quantity == null ? 1 : it.quantity);
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity()));
            oi.setSubtotal(subtotal);
            orderItemMapper.insert(oi);
            savedItems.add(oi);
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return ApiResponse.ok(order);
    }
    
    @Override
    public ApiResponse<Order> getOrderById(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null) throw new java.util.NoSuchElementException("订单不存在");
        return ApiResponse.ok(o);
    }
    
    @Override
    public ApiResponse<org.springframework.data.domain.Page<Order>> getUserOrders(Long userId, int page, int size) {
        Page<Order> mp = orderMapper.selectPage(new Page<>(page + 1L, size),
                new QueryWrapper<Order>().eq("buyer_id", userId).orderByDesc("created_at"));
        return ApiResponse.ok(new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal()));
    }
    
    @Override
    public ApiResponse<org.springframework.data.domain.Page<Order>> getMerchantOrders(Long shopId, int page, int size) {
        Page<Order> mp = orderMapper.selectPage(new Page<>(page + 1L, size),
                new QueryWrapper<Order>().eq("shop_id", shopId).orderByDesc("created_at"));
        return ApiResponse.ok(new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal()));
    }

    @Override
    public ApiResponse<org.springframework.data.domain.Page<Order>> listAllOrders(int page, int size) {
        Page<Order> mp = orderMapper.selectPage(new Page<>(page + 1L, size),
                new QueryWrapper<Order>().orderByDesc("created_at"));
        return ApiResponse.ok(new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal()));
    }
    
    @Override
    public ApiResponse<Void> updateOrderStatus(Long orderId, String status) {
        Order o = orderMapper.selectById(orderId);
        if (o == null) throw new java.util.NoSuchElementException("订单不存在");
        try { o.setStatus(OrderStatus.valueOf(status)); } catch (IllegalArgumentException e) { throw new IllegalArgumentException("非法状态"); }
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        return ApiResponse.ok();
    }
    
    @Override
    public ApiResponse<Void> cancelOrder(Long orderId, Long userId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null) throw new java.util.NoSuchElementException("订单不存在");
        if (!o.getUserId().equals(userId)) throw new IllegalArgumentException("无权取消");
        o.setStatus(OrderStatus.CANCELLED);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        return ApiResponse.ok();
    }
    
    @Override
    public ApiResponse<List<OrderItem>> getOrderItems(Long orderId) {
        List<OrderItem> list = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        return ApiResponse.ok(list);
    }
    
    @Override
    public ApiResponse<Void> confirmOrder(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null) throw new java.util.NoSuchElementException("订单不存在");
        o.setStatus(OrderStatus.CONFIRMED);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        return ApiResponse.ok();
    }
    
    @Override
    public ApiResponse<Void> shipOrder(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null) throw new java.util.NoSuchElementException("订单不存在");
        o.setStatus(OrderStatus.SHIPPED);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        return ApiResponse.ok();
    }
    
    @Override
    public ApiResponse<Void> completeOrder(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null) throw new java.util.NoSuchElementException("订单不存在");
        o.setStatus(OrderStatus.COMPLETED);
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        return ApiResponse.ok();
    }
}