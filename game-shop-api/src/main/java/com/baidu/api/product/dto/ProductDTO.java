package com.baidu.api.product.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private Long shopId;
    private String title;
    private BigDecimal price;
    private String description;
    private String coverUrl;
    private String category;
    private String status;
}

