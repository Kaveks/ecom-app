
package org.example.ecomapp.dto.cartitem;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CartItemRequest {

    @NotNull(message = "Quantity is required.")
    @Positive(message = "Quantity must be greater than zero.")
    private Integer quantity;

    @NotNull(message = "Product Id  is required.")
    @Positive(message = "Product Id must be greater than zero.")
    private Long productId;
}
