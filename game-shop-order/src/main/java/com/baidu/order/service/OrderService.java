package com.baidu.order.service;

import com.baidu.common.ApiResponse;
import com.baidu.order.entity.Order;
import com.baidu.order.entity.OrderItem;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单（支持多商品）
     */
    ApiResponse<Order> createOrder(Long userId, com.baidu.order.controller.OrderController.OrderCreateRequest req);
    
    /**
     * 根据ID查询订单
     */
    ApiResponse<Order> getOrderById(Long id);
    
    /**
     * 查询用户订单列表
     */
    ApiResponse<Page<Order>> getUserOrders(Long userId, int page, int size);
    
    /**
     * 查询商家订单列表（按店铺）
     */
    ApiResponse<Page<Order>> getMerchantOrders(Long shopId, int page, int size);

    /**
     * 查询所有订单（兼容模式）
     */
    ApiResponse<Page<Order>> listAllOrders(int page, int size);
    
    /**
     * 更新订单状态
     */
    ApiResponse<Void> updateOrderStatus(Long orderId, String status);
    
    /**
     * 取消订单
     */
    ApiResponse<Void> cancelOrder(Long orderId, Long userId);
    
    /**
     * 查询订单项
     */
    ApiResponse<List<OrderItem>> getOrderItems(Long orderId);
    
    /**
     * 确认订单
     */
    ApiResponse<Void> confirmOrder(Long orderId);
    
    /**
     * 发货
     */
    ApiResponse<Void> shipOrder(Long orderId);
    
    /**
     * 完成订单
     */
    ApiResponse<Void> completeOrder(Long orderId);
}
