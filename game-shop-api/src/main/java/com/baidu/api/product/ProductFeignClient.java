package com.baidu.api.product;

import com.baidu.common.ApiResponse;
import com.baidu.api.product.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "game-shop-product", path = "/api/products")
public interface ProductFeignClient {
    
    @GetMapping("/{productId}")
    ApiResponse<ProductDTO> getProductById(@PathVariable("productId") Long productId);
}

