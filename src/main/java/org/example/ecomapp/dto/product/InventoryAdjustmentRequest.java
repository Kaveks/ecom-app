package org.example.ecomapp.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InventoryAdjustmentRequest {

    @NotNull(message = "Counted quantity is required.")
    @PositiveOrZero(message = "Counted quantity cannot be negative.")
    private Integer countedQuantity;

    @NotBlank(message = "Reason is required.")
    @Size(max = 500, message = "Reason cannot exceed 500 characters.")
    private String reason;
}