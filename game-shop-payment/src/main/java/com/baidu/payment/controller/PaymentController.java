package com.baidu.payment.controller;

import com.baidu.common.ApiResponse;
import com.baidu.payment.entity.Payment;
import com.baidu.payment.service.PaymentService;
import com.baidu.security.RequireRoles;
import com.baidu.security.Role;
import com.baidu.security.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    private Long currentUserId() {
        Long id = RequestContext.getUserId();
        if (id == null) throw new IllegalArgumentException("用户ID不能为空");
        return id;
    }

    // 创建支付
    @PostMapping("/create")
    public ApiResponse<Payment> createPayment(@RequestBody CreatePaymentRequest req) {
        Long userId = currentUserId();
        return paymentService.createPayment(req.orderId, userId, req.amount, req.method);
    }

    // 查询支付状态
    @GetMapping("/{paymentId}")
    public ApiResponse<Payment> getPaymentById(@PathVariable Long paymentId) {
        return paymentService.getPaymentById(paymentId);
    }
    
    // 根据订单ID查询支付（原路径）
    @GetMapping("/order/{orderId}")
    public ApiResponse<Payment> getPaymentByOrderId(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }

    // 根据订单ID查询支付（对齐前端：/by-order/{orderId}）
    @GetMapping("/by-order/{orderId}")
    public ApiResponse<Payment> getPaymentByOrderIdAlias(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }
    
    // 支付回调接口
    @PostMapping("/callback")
    public ApiResponse<Void> paymentCallback(@RequestBody PaymentCallbackRequest req) {
        return paymentService.handlePaymentCallback(req.transactionId, req.thirdPartyId, req.status);
    }
    
    // 查询我的支付记录
    @RequireRoles({Role.BUYER})
    @GetMapping("/my")
    public ApiResponse<Page<Payment>> myPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = currentUserId();
        return paymentService.getUserPayments(userId, page, size);
    }
    
    // 商家查询收款记录
    @RequireRoles({Role.MERCHANT})
    @GetMapping("/merchant")
    public ApiResponse<Page<Payment>> merchantPayments(
            @RequestParam Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return paymentService.getMerchantPayments(shopId, page, size);
    }
    
    // 取消支付
    @PutMapping("/{paymentId}/cancel")
    public ApiResponse<Void> cancelPayment(@PathVariable Long paymentId) {
        return paymentService.cancelPayment(paymentId);
    }
    
    // 确认支付
    @PutMapping("/{paymentId}/confirm")
    public ApiResponse<Void> confirmPayment(@PathVariable Long paymentId, @RequestBody ConfirmPaymentRequest req) {
        return paymentService.confirmPayment(paymentId, req.transactionId);
    }

    // 对齐前端：/payments/{paymentId}/pay
    @PostMapping("/{paymentId}/pay")
    public ApiResponse<Void> pay(@PathVariable Long paymentId) {
        // 简化：自动生成 transactionId 并标记支付成功
        return paymentService.confirmPayment(paymentId, java.util.UUID.randomUUID().toString());
    }
    
    // 管理员：更新支付状态
    @RequireRoles({Role.ADMIN})
    @PutMapping("/{paymentId}/status")
    public ApiResponse<Void> updatePaymentStatus(@PathVariable Long paymentId, @RequestParam String status) {
        return paymentService.updatePaymentStatus(paymentId, status);
    }
    
    // 管理员：退款
    @RequireRoles({Role.ADMIN})
    @PutMapping("/{paymentId}/refund")
    public ApiResponse<Void> refundPayment(@PathVariable Long paymentId, @RequestBody RefundRequest req) {
        return paymentService.refundPayment(paymentId, req.amount, req.reason);
    }

    public static class CreatePaymentRequest {
        public Long orderId;
        public BigDecimal amount;
        public String method;
    }
    
    public static class PaymentCallbackRequest {
        public String transactionId;
        public String thirdPartyId;
        public String status;
    }
    
    public static class ConfirmPaymentRequest {
        public String transactionId;
    }
    
    public static class RefundRequest {
        public BigDecimal amount;
        public String reason;
    }
}
