package com.baidu.api.shop;

import com.baidu.common.ApiResponse;
import com.baidu.api.shop.dto.ShopDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "game-shop-shop", path = "/api/shops")
public interface ShopFeignClient {
    
    @GetMapping("/{shopId}")
    ApiResponse<ShopDTO> getShopById(@PathVariable("shopId") Long shopId);
    
    @GetMapping("/owner/{ownerId}")
    ApiResponse<ShopDTO> getShopByOwnerId(@PathVariable("ownerId") Long ownerId);
}

