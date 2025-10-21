package com.baidu.payment.service;

import com.baidu.common.ApiResponse;
import com.baidu.payment.entity.Payment;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付服务接口
 */
public interface PaymentService {
    
    /**
     * 创建支付
     */
    ApiResponse<Payment> createPayment(Long orderId, Long userId, BigDecimal amount, String paymentMethod);
    
    /**
     * 根据ID查询支付
     */
    ApiResponse<Payment> getPaymentById(Long id);
    
    /**
     * 根据订单ID查询支付
     */
    ApiResponse<Payment> getPaymentByOrderId(Long orderId);
    
    /**
     * 处理支付回调
     */
    ApiResponse<Void> handlePaymentCallback(String transactionId, String thirdPartyId, String status);
    
    /**
     * 更新支付状态
     */
    ApiResponse<Void> updatePaymentStatus(Long paymentId, String status);
    
    /**
     * 查询用户支付记录
     */
    ApiResponse<Page<Payment>> getUserPayments(Long userId, int page, int size);
    
    /**
     * 查询商家收款记录
     */
    ApiResponse<Page<Payment>> getMerchantPayments(Long shopId, int page, int size);
    
    /**
     * 退款
     */
    ApiResponse<Void> refundPayment(Long paymentId, BigDecimal refundAmount, String reason);
    
    /**
     * 取消支付
     */
    ApiResponse<Void> cancelPayment(Long paymentId);
    
    /**
     * 确认支付
     */
    ApiResponse<Void> confirmPayment(Long paymentId, String transactionId);
}
