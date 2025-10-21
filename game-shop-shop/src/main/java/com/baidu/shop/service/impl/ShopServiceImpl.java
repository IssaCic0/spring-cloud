package com.baidu.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baidu.shop.mapper.ShopMapper;
import com.baidu.shop.entity.Shop;
import com.baidu.shop.entity.ShopStatus;
import com.baidu.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired 
    private ShopMapper shopMapper;

    @Override
    @Transactional
    public Shop openShop(Long ownerId, String name, String description, String logoUrl) {
        Shop shop = new Shop();
        shop.setOwnerId(ownerId);
        shop.setName(name);
        shop.setDescription(description);
        shop.setLogoUrl(logoUrl);
        shop.setStatus(ShopStatus.PENDING_APPROVAL);
        shopMapper.insert(shop);
        return shop;
    }

    @Override
    @Transactional
    public void closeShop(Long ownerId) {
        Shop shop = shopMapper.selectOne(new QueryWrapper<Shop>().eq("owner_id", ownerId));
        if (shop == null) throw new NoSuchElementException("店铺不存在");
        shop.setStatus(ShopStatus.CLOSED);
        shopMapper.updateById(shop);
    }

    @Override
    @Transactional
    public void closeMyShop(Long ownerId, Long shopId) {
        Shop s = getMyShop(ownerId, shopId);
        s.setStatus(ShopStatus.CLOSED);
        shopMapper.updateById(s);
    }

    @Override
    @Transactional
    public Shop updateShop(Long ownerId, String name, String description, String logoUrl) {
        Shop shop = shopMapper.selectOne(new QueryWrapper<Shop>().eq("owner_id", ownerId));
        if (shop == null) throw new NoSuchElementException("店铺不存在");
        if (name != null) shop.setName(name);
        if (description != null) shop.setDescription(description);
        if (logoUrl != null) shop.setLogoUrl(logoUrl);
        shopMapper.updateById(shop);
        return shop;
    }

    @Override
    public Shop getMyShop(Long ownerId) {
        Shop shop = shopMapper.selectOne(new QueryWrapper<Shop>().eq("owner_id", ownerId));
        if (shop == null) throw new NoSuchElementException("店铺不存在");
        return shop;
    }

    @Override
    @Transactional
    public void deleteMyShop(Long ownerId) {
        Shop shop = shopMapper.selectOne(new QueryWrapper<Shop>().eq("owner_id", ownerId));
        if (shop == null) throw new NoSuchElementException("店铺不存在");
        shopMapper.deleteById(shop.getId());
    }

    @Override
    @Transactional
    public void deleteMyShop(Long ownerId, Long shopId) {
        Shop s = getMyShop(ownerId, shopId);
        shopMapper.deleteById(s.getId());
    }

    @Override
    public org.springframework.data.domain.Page<Shop> adminList(int page, int size, Long ownerId, String name) {
        QueryWrapper<Shop> wrapper = new QueryWrapper<>();
        if (ownerId != null) {
            wrapper.eq("owner_id", ownerId);
        }
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("name", name);
        }
        wrapper.orderByDesc("created_at");
        
        Page<Shop> mp = shopMapper.selectPage(new Page<>(page + 1L, size), wrapper);
        return new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal());
    }

    @Override
    public org.springframework.data.domain.Page<Shop> listMyShops(Long ownerId, int page, int size) {
        Page<Shop> mp = shopMapper.selectPage(new Page<>(page + 1L, size), new QueryWrapper<Shop>()
                .eq("owner_id", ownerId)
                .orderByDesc("created_at"));
        return new PageImpl<>(mp.getRecords(), PageRequest.of(page, size), mp.getTotal());
    }

    @Override
    @Transactional
    public Shop createShop(Long ownerId, String name, String description, String logoUrl) {
        Shop s = new Shop();
        s.setOwnerId(ownerId);
        s.setName(name);
        s.setDescription(description);
        s.setLogoUrl(logoUrl);
        s.setStatus(ShopStatus.PENDING_APPROVAL);
        shopMapper.insert(s);
        return s;
    }

    @Override
    public Shop getMyShop(Long ownerId, Long shopId) {
        Shop s = shopMapper.selectById(shopId);
        if (s == null || !s.getOwnerId().equals(ownerId)) {
            throw new NoSuchElementException("店铺不存在");
        }
        return s;
    }

    @Override
    @Transactional
    public void updateMyShop(Long ownerId, Long shopId, String name, String description, String logoUrl) {
        Shop s = getMyShop(ownerId, shopId);
        if (name != null) s.setName(name);
        if (description != null) s.setDescription(description);
        if (logoUrl != null) s.setLogoUrl(logoUrl);
        shopMapper.updateById(s);
    }

    @Override
    @Transactional
    public void approve(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) throw new NoSuchElementException("店铺不存在");
        shop.setStatus(ShopStatus.OPEN);
        shopMapper.updateById(shop);
    }

    @Override
    @Transactional
    public void ban(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) throw new NoSuchElementException("店铺不存在");
        shop.setStatus(ShopStatus.BANNED);
        shopMapper.updateById(shop);
    }

    @Override
    @Transactional
    public Shop adminCreate(Long ownerId, String name, String description, String logoUrl, ShopStatus status) {
        Shop shop = new Shop();
        shop.setOwnerId(ownerId);
        shop.setName(name);
        shop.setDescription(description);
        shop.setLogoUrl(logoUrl);
        shop.setStatus(status != null ? status : ShopStatus.PENDING_APPROVAL);
        shopMapper.insert(shop);
        return shop;
    }

    @Override
    @Transactional
    public Shop adminUpdate(Long shopId, String name, String description, String logoUrl, ShopStatus status) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) throw new NoSuchElementException("店铺不存在");
        if (name != null) shop.setName(name);
        if (description != null) shop.setDescription(description);
        if (logoUrl != null) shop.setLogoUrl(logoUrl);
        if (status != null) shop.setStatus(status);
        shopMapper.updateById(shop);
        return shop;
    }

    @Override
    @Transactional
    public void adminDelete(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) throw new NoSuchElementException("店铺不存在");
        shopMapper.deleteById(shopId);
    }

    @Override
    public Shop adminGet(Long shopId) {
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) throw new NoSuchElementException("店铺不存在");
        return shop;
    }
}
