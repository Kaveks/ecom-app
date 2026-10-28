package org.example.ecomapp.dto.product;

import lombok.Data;

@Data
public class InventoryStockRequest {
    private Integer quantity;
    private String reason;
}