package com.baidu.api.shop.dto;

import lombok.Data;

@Data
public class ShopDTO {
    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private String logoUrl;
    private String status;
}

