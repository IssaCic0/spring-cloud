package com.baidu.product.controller;

import com.baidu.common.ApiResponse;
import com.baidu.product.entity.Product;
import com.baidu.product.service.ProductService;
import com.baidu.security.RequireRoles;
import com.baidu.security.Role;
import com.baidu.security.RequestContext;
import com.baidu.api.shop.ShopFeignClient;
import com.baidu.api.shop.dto.ShopDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductsController {
    
    private final ProductService productService;
    private final ShopFeignClient shopFeignClient;

    private Long currentUserId() {
        Long id = RequestContext.getUserId();
        if (id == null) throw new IllegalArgumentException("用户ID不能为空");
        return id;
    }

    // 商家：创建商品
    @RequireRoles({Role.MERCHANT})
    @PostMapping
    public ApiResponse<Product> createProduct(@RequestBody ProductCreateRequest req) {
        // 构建商品对象
        Product product = new Product();
        product.setShopId(req.shopId);
        product.setTitle(req.title);
        product.setPrice(req.price);
        product.setDescription(req.description);
        product.setCoverUrl(req.coverUrl);
        product.setCategory(req.category);
        
        return productService.createProduct(product);
    }

    // 公开：查询商品详情
    @GetMapping("/{productId}")
    public ApiResponse<Product> getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }

    // 公开：查询商品列表
    @GetMapping
    public ApiResponse<Page<Product>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return productService.listProducts(page, size, category, keyword);
    }

    // 管理员：分页列表（对齐前端 /api/products/admin）
    @RequireRoles({Role.ADMIN})
    @GetMapping("/admin")
    public ApiResponse<java.util.Map<String, Object>> adminList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long shopId) {
        Page<Product> p = productService.adminList(page, size, keyword, shopId);
        java.util.Map<String, Object> r = new java.util.HashMap<>();
        r.put("page", p.getNumber());
        r.put("size", p.getSize());
        r.put("total", p.getTotalElements());
        r.put("items", p.getContent());
        return ApiResponse.ok(r);
    }

    // 管理员：强制下线（对齐前端 /api/products/admin/{productId}/force-offline）
    @RequireRoles({Role.ADMIN})
    @PostMapping("/admin/{productId}/force-offline")
    public ApiResponse<Void> adminForceOffline(@PathVariable Long productId) {
        return productService.updateProductStatus(productId, "INACTIVE");
    }

    // 管理员：详情（对齐前端）
    @RequireRoles({Role.ADMIN})
    @GetMapping("/admin/{productId}")
    public ApiResponse<Product> adminGet(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }

    // 管理员：更新（对齐前端）
    @RequireRoles({Role.ADMIN})
    @PutMapping("/admin/{productId}")
    public ApiResponse<Product> adminUpdate(@PathVariable Long productId, @RequestBody ProductCreateRequest req) {
        Product product = new Product();
        product.setShopId(req.shopId);
        product.setTitle(req.title);
        product.setPrice(req.price);
        product.setDescription(req.description);
        product.setCoverUrl(req.coverUrl);
        product.setCategory(req.category);
        return productService.updateProduct(productId, product);
    }

    // 管理员：删除（对齐前端）
    @RequireRoles({Role.ADMIN})
    @DeleteMapping("/admin/{productId}")
    public ApiResponse<Void> adminDelete(@PathVariable Long productId) {
        return productService.deleteProduct(productId);
    }

    // 管理员：创建（对齐前端）
    @RequireRoles({Role.ADMIN})
    @PostMapping("/admin")
    public ApiResponse<Product> adminCreate(@RequestBody ProductCreateRequest req) {
        Product product = new Product();
        product.setShopId(req.shopId);
        product.setTitle(req.title);
        product.setPrice(req.price);
        product.setDescription(req.description);
        product.setCoverUrl(req.coverUrl);
        product.setCategory(req.category);
        return productService.createProduct(product);
    }
    
    // 商家：更新商品
    @RequireRoles({Role.MERCHANT})
    @PutMapping("/{productId}")
    public ApiResponse<Product> updateProduct(@PathVariable Long productId, @RequestBody ProductCreateRequest req) {
        // 构建商品对象
        Product product = new Product();
        product.setShopId(req.shopId);
        product.setTitle(req.title);
        product.setPrice(req.price);
        product.setDescription(req.description);
        product.setCoverUrl(req.coverUrl);
        product.setCategory(req.category);
        
        return productService.updateProduct(productId, product);
    }
    
    // 商家：删除商品
    @RequireRoles({Role.MERCHANT})
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long productId) {
        return productService.deleteProduct(productId);
    }
    
    // 根据店铺ID查询商品
    @GetMapping("/shop/{shopId}")
    public ApiResponse<List<Product>> getProductsByShopId(@PathVariable Long shopId) {
        return productService.getProductsByShopId(shopId);
    }

    // 商家：我的商品（对齐前端：/api/products/me?page=&size=）
    @RequireRoles({Role.MERCHANT})
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> myProducts(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Long uid = RequestContext.getUserId();
        if (uid == null) throw new IllegalArgumentException("用户ID不能为空");
        Long sid = null;
        try {
            ShopDTO shop = shopFeignClient.getShopByOwnerId(uid).getData();
            sid = (shop == null ? null : shop.getId());
        } catch (Exception ignore) { /* 降级：若店铺服务未启动，则不按店铺过滤 */ }
        Page<Product> p = productService.adminList(page, size, null, sid);
        Map<String, Object> r = new HashMap<>();
        r.put("page", p.getNumber());
        r.put("size", p.getSize());
        r.put("total", p.getTotalElements());
        r.put("items", p.getContent());
        return ApiResponse.ok(r);
    }

    // 商家：销量统计（对齐前端：ids=1,2,3）
    @RequireRoles({Role.MERCHANT})
    @GetMapping("/merchant/sales")
    public ApiResponse<Map<String, Object>> merchantSales(@RequestParam("ids") List<Long> ids) {
        Map<Long, Long> sales = new HashMap<>();
        if (ids != null) {
            for (Long id : ids) sales.put(id, 0L);
        }
        Map<String, Object> r = new HashMap<>();
        r.put("sales", sales);
        return ApiResponse.ok(r);
    }
    
    // 根据分类查询商品
    @GetMapping("/category/{category}")
    public ApiResponse<List<Product>> getProductsByCategory(@PathVariable String category) {
        return productService.getProductsByCategory(category);
    }
    
    // 搜索商品（对齐前端：返回 {items,total}，支持分页与分类）
    @GetMapping("/search")
    public ApiResponse<Map<String,Object>> searchProducts(@RequestParam(required = false) String keyword,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "12") int size,
                                                          @RequestParam(required = false) String category) {
        Page<Product> p = productService.listProducts(page, size, category, keyword).getData();
        Map<String,Object> r = new HashMap<>();
        if(p!=null){
            r.put("items", p.getContent());
            r.put("total", p.getTotalElements());
        }else{
            r.put("items", java.util.Collections.emptyList());
            r.put("total", 0);
        }
        return ApiResponse.ok(r);
    }
    
    // 管理员：更新商品状态
    @RequireRoles({Role.ADMIN})
    @PutMapping("/{productId}/status")
    public ApiResponse<Void> updateProductStatus(@PathVariable Long productId, @RequestParam String status) {
        return productService.updateProductStatus(productId, status);
    }

    public static class ProductCreateRequest {
        public Long shopId;
        public String title;
        public BigDecimal price;
        public String description;
        public String coverUrl;
        public String category;
    }
}
