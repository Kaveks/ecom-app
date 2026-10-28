package org.example.ecomapp.dto.product;

import lombok.Data;

@Data
public class InventoryResponse {
    private String productId;
    private String productName;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer availableStock;
}