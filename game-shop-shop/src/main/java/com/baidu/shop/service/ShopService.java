package com.baidu.shop.service;

import com.baidu.shop.entity.Shop;
import com.baidu.shop.entity.ShopStatus;
import org.springframework.data.domain.Page;

public interface ShopService {

    Shop openShop(Long ownerId, String name, String description, String logoUrl);

    void closeShop(Long ownerId);

    Shop updateShop(Long ownerId, String name, String description, String logoUrl);

    Shop getMyShop(Long ownerId);

    void deleteMyShop(Long ownerId);

    // 商家多店
    Page<Shop> listMyShops(Long ownerId, int page, int size);
    Shop createShop(Long ownerId, String name, String description, String logoUrl);
    Shop getMyShop(Long ownerId, Long shopId);
    void updateMyShop(Long ownerId, Long shopId, String name, String description, String logoUrl);
    void closeMyShop(Long ownerId, Long shopId);
    void deleteMyShop(Long ownerId, Long shopId);

    // 管理
    Page<Shop> adminList(int page, int size, Long ownerId, String name);

    void approve(Long shopId);

    void ban(Long shopId);

    Shop adminCreate(Long ownerId, String name, String description, String logoUrl, ShopStatus status);

    Shop adminUpdate(Long shopId, String name, String description, String logoUrl, ShopStatus status);

    void adminDelete(Long shopId);

    Shop adminGet(Long shopId);
}
