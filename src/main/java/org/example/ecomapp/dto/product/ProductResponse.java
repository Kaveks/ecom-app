package org.example.ecomapp.dto.product;

import lombok.Data;
import org.example.ecomapp.enums.ProductCategory;

import java.math.BigDecimal;

@Data
public class ProductResponse {

    private String id;
    private String name;
    private BigDecimal price;
    private Integer availableStock;
    private ProductCategory category;
    private String description;
    private String imageUrl;
    private Boolean active;
}