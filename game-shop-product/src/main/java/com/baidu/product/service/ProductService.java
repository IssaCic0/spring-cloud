package com.baidu.product.service;

import com.baidu.common.ApiResponse;
import com.baidu.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {
    
    /**
     * 创建商品
     */
    ApiResponse<Product> createProduct(Product product);
    
    /**
     * 根据ID查询商品
     */
    ApiResponse<Product> getProductById(Long id);
    
    /**
     * 更新商品信息
     */
    ApiResponse<Product> updateProduct(Long id, Product product);
    
    /**
     * 删除商品
     */
    ApiResponse<Void> deleteProduct(Long id);
    
    /**
     * 分页查询商品列表
     */
    ApiResponse<Page<Product>> listProducts(int page, int size, String category, String keyword);
    
    /** 管理端分页（支持关键词与店铺筛选） */
    Page<Product> adminList(int page, int size, String keyword, Long shopId);
    
    /**
     * 根据店铺ID查询商品
     */
    ApiResponse<List<Product>> getProductsByShopId(Long shopId);
    
    /**
     * 根据分类查询商品
     */
    ApiResponse<List<Product>> getProductsByCategory(String category);
    
    /**
     * 搜索商品
     */
    ApiResponse<List<Product>> searchProducts(String keyword);
    
    /**
     * 更新商品状态
     */
    ApiResponse<Void> updateProductStatus(Long id, String status);
}
