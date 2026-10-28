package org.example.ecomapp.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InventoryStockRequest {
    @NotNull(message = "Quantity is required.")
    @Positive(message = "Quantity must be greater than zero.")
    private Integer quantity;

    @NotBlank(message = "Reason is required.")
    @Size(max = 500, message = "Reason cannot exceed 500 characters.")
    private String reason;
}