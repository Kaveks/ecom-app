
package org.example.ecomapp.service.product;

import org.example.ecomapp.model.Inventory;
import org.example.ecomapp.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductValidationService {

    public void validateProductAvailability(Product product) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    product.getName() + " is currently unavailable."
            );
        }

        if (product.getPrice() == null
                || product.getPrice().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    product.getName() + " has an invalid price."
            );
        }
    }

    public void validateStock(Inventory inventory, int requestedQuantity) {
        validateQuantity(requestedQuantity);

        if (inventory.getAvailableStock() < requestedQuantity) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only " + inventory.getAvailableStock()
                            + " units of "
                            + inventory.getProduct().getName()
                            + " are currently available."
            );
        }
    }

    public void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero."
            );
        }
    }
}
