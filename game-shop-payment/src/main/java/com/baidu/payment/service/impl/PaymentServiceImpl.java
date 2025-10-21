package com.baidu.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baidu.common.ApiResponse;
import com.baidu.payment.entity.Payment;
import com.baidu.payment.entity.PaymentStatus;
import com.baidu.payment.mapper.PaymentMapper;
import com.baidu.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public ApiResponse<Payment> createPayment(Long orderId, Long userId, BigDecimal amount, String paymentMethod) {
        // 如果该订单已有支付记录则直接返回
        Payment existed = paymentMapper.selectOne(new QueryWrapper<Payment>().eq("order_id", orderId));
        if (existed != null) return ApiResponse.ok(existed);
        Payment p = new Payment();
        p.setOrderId(orderId);
        p.setUserId(userId);
        p.setAmount(amount);
        p.setPaymentMethod(paymentMethod);
        p.setStatus(PaymentStatus.PENDING);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        paymentMapper.insert(p);
        return ApiResponse.ok(p);
    }
    
    @Override
    public ApiResponse<Payment> getPaymentById(Long id) {
        Payment p = paymentMapper.selectById(id);
        if (p == null) throw new java.util.NoSuchElementException("支付不存在");
        return ApiResponse.ok(p);
    }
    
    @Override
    public ApiResponse<Payment> getPaymentByOrderId(Long orderId) {
        Payment p = paymentMapper.selectOne(new QueryWrapper<Payment>().eq("order_id", orderId));
        if (p == null) {
            // 懒创建空支付单，金额待客户端或后续计算
            p = new Payment();
            p.setOrderId(orderId);
            p.setStatus(PaymentStatus.PENDING);
            paymentMapper.insert(p);
        }
        return ApiResponse.ok(p);
    }
    
    @Override
    public ApiResponse<Void> handlePaymentCallback(String transactionId, String thirdPartyId, String status) {
        Payment p = paymentMapper.selectOne(new QueryWrapper<Payment>().eq("transaction_id", transactionId));
        if (p == null) return ApiResponse.ok();
        if (thirdPartyId != null) p.setThirdPartyId(thirdPartyId);
        try { p.setStatus(PaymentStatus.valueOf(status)); } catch (IllegalArgumentException ignored) {}
        if (p.getStatus() == PaymentStatus.SUCCESS) {
            p.setPaymentTime(LocalDateTime.now());
        }
        p.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(p);
        return ApiResponse.ok();
    }
    
    @Override
    public ApiResponse<Void> updatePaymentStatus(Long paymentId, String status) {
        Payment p = paymentMapper.selectById(paymentId);
        if (p == null) throw new java.util.NoSuchElementException("支付不存在");
        try { p.setStatus(PaymentStatus.valueOf(status)); } catch (IllegalArgumentException e) { throw new IllegalArgumentException("非法状态"); }
        p.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(p);
        return ApiResponse.ok();
    }
    
    @Override
    public ApiResponse<org.springframework.data.domain.Page<Payment>> getUserPayments(Long userId, int page, int size) {
        Page<Payment> mp = paymentMapper.selectPage(new Page<>(page + 1L, size),
                new QueryWrapper<Payment>().eq("user_id", userId).orderByDesc("created_at"));
        return ApiResponse.ok(new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal()));
    }
    
    @Override
    public ApiResponse<org.springframework.data.domain.Page<Payment>> getMerchantPayments(Long shopId, int page, int size) {
        // 暂无法按店铺直接分页（无 shop_id 字段），返回空页
        return ApiResponse.ok(new PageImpl<>(java.util.Collections.emptyList(), PageRequest.of(page, size), 0));
    }
    
    @Override
    public ApiResponse<Void> refundPayment(Long paymentId, BigDecimal refundAmount, String reason) {
        Payment p = paymentMapper.selectById(paymentId);
        if (p == null) throw new java.util.NoSuchElementException("支付不存在");
        p.setStatus(PaymentStatus.REFUNDED);
        p.setRemark(reason);
        p.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(p);
        return ApiResponse.ok();
    }
    
    @Override
    public ApiResponse<Void> cancelPayment(Long paymentId) {
        Payment p = paymentMapper.selectById(paymentId);
        if (p == null) throw new java.util.NoSuchElementException("支付不存在");
        p.setStatus(PaymentStatus.CANCELLED);
        p.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(p);
        return ApiResponse.ok();
    }
    
    @Override
    public ApiResponse<Void> confirmPayment(Long paymentId, String transactionId) {
        Payment p = paymentMapper.selectById(paymentId);
        if (p == null) throw new java.util.NoSuchElementException("支付不存在");
        p.setTransactionId(transactionId);
        p.setStatus(PaymentStatus.SUCCESS);
        p.setPaymentTime(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(p);
        return ApiResponse.ok();
    }
}