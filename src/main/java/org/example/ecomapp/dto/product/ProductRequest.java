package org.example.ecomapp.dto.product;

import lombok.Data;
import org.example.ecomapp.enums.ProductCategory;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    private String name;
    private BigDecimal price;
    private ProductCategory category;
    private String description;
    private String imageUrl;
}