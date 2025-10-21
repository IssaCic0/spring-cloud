package com.baidu.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baidu.common.ApiResponse;
import com.baidu.product.entity.Product;
import com.baidu.product.entity.ProductStatus;
import com.baidu.product.mapper.ProductMapper;
import com.baidu.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ApiResponse<Product> createProduct(Product product) {
        if (product.getStatus() == null) product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);
        return ApiResponse.ok(product);
    }

    @Override
    public ApiResponse<Product> getProductById(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new java.util.NoSuchElementException("商品不存在");
        return ApiResponse.ok(p);
    }

    @Override
    @Transactional
    public ApiResponse<Product> updateProduct(Long id, Product product) {
        Product existed = productMapper.selectById(id);
        if (existed == null) throw new java.util.NoSuchElementException("商品不存在");
        if (product.getShopId() != null) existed.setShopId(product.getShopId());
        if (product.getTitle() != null) existed.setTitle(product.getTitle());
        if (product.getPrice() != null) existed.setPrice(product.getPrice());
        if (product.getDescription() != null) existed.setDescription(product.getDescription());
        if (product.getCoverUrl() != null) existed.setCoverUrl(product.getCoverUrl());
        if (product.getCategory() != null) existed.setCategory(product.getCategory());
        existed.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(existed);
        return ApiResponse.ok(existed);
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteProduct(Long id) {
        Product existed = productMapper.selectById(id);
        if (existed == null) return ApiResponse.ok();
        existed.setStatus(ProductStatus.DELETED);
        existed.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(existed);
        return ApiResponse.ok();
    }

    @Override
    public ApiResponse<org.springframework.data.domain.Page<Product>> listProducts(int page, int size, String category, String keyword) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        qw.eq("status", ProductStatus.ACTIVE.name());
        if (category != null && !category.isBlank()) {
            qw.eq("category", category);
        }
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("title", keyword).or().like("description", keyword));
        }
        qw.orderByDesc("created_at");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> mp = productMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page + 1L, size), qw);
        return ApiResponse.ok(new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal()));
    }

    @Override
    public Page<Product> adminList(int page, int size, String keyword, Long shopId) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        if (shopId != null) qw.eq("shop_id", shopId);
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("title", keyword).or().like("description", keyword));
        }
        qw.orderByDesc("created_at");
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Product> mp = productMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page + 1L, size), qw);
        return new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal());
    }

    @Override
    public ApiResponse<List<Product>> getProductsByShopId(Long shopId) {
        List<Product> list = productMapper.selectList(new QueryWrapper<Product>()
                .eq("shop_id", shopId)
                .eq("status", ProductStatus.ACTIVE.name())
                .orderByDesc("created_at"));
        return ApiResponse.ok(list);
    }

    @Override
    public ApiResponse<List<Product>> getProductsByCategory(String category) {
        List<Product> list = productMapper.selectList(new QueryWrapper<Product>()
                .eq("category", category)
                .eq("status", ProductStatus.ACTIVE.name())
                .orderByDesc("created_at"));
        return ApiResponse.ok(list);
    }

    @Override
    public ApiResponse<List<Product>> searchProducts(String keyword) {
        QueryWrapper<Product> qw = new QueryWrapper<>();
        qw.eq("status", ProductStatus.ACTIVE.name());
        if (keyword != null && !keyword.isBlank()) {
            qw.and(w -> w.like("title", keyword).or().like("description", keyword));
        }
        List<Product> list = productMapper.selectList(qw.orderByDesc("created_at"));
        return ApiResponse.ok(list);
    }

    @Override
    @Transactional
    public ApiResponse<Void> updateProductStatus(Long id, String status) {
        Product existed = productMapper.selectById(id);
        if (existed == null) throw new java.util.NoSuchElementException("商品不存在");
        try {
            existed.setStatus(ProductStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("非法状态");
        }
        existed.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(existed);
        return ApiResponse.ok();
    }
}
