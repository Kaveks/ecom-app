package org.example.ecomapp.service.inventory;

import org.example.ecomapp.enums.InventoryMovementType;
import org.example.ecomapp.model.Inventory;
import org.example.ecomapp.model.InventoryMovement;
import org.example.ecomapp.repository.InventoryMovementRepository;
import org.example.ecomapp.repository.InventoryRepository;
import org.example.ecomapp.service.product.ProductValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductValidationService productValidationService;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryMovementRepository inventoryMovementRepository,
            ProductValidationService productValidationService
    ) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.productValidationService = productValidationService;
    }

    @Transactional(readOnly = true)
    public Inventory fetchInventory(Long productId) {
        return inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory was not found for product ID "
                                + productId + "."
                ));
    }

    @Transactional
    public void processStock(
            Long productId,
            Integer quantity,
            InventoryMovementType type,
            String reason
    ) {
        if (productId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product ID is required."
            );
        }

        if (type == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Inventory movement type is required."
            );
        }

        validateMovementQuantity(quantity, type);

        Inventory inventory = inventoryRepository
                .findByProductIdForUpdate(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory was not found for product ID "
                                + productId + "."
                ));

        int onHand = inventory.getQuantityOnHand();
        int reserved = inventory.getQuantityReserved();

        if (reserved < 0 || reserved > onHand) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Inventory quantities are inconsistent."
            );
        }

        try {
            switch (type) {
                case STOCK_RECEIVED, STOCK_RETURNED -> {
                    inventory.setQuantityOnHand(
                            Math.addExact(onHand, quantity)
                    );
                }

                case STOCK_RESERVED -> {
                    productValidationService.validateStock(
                            inventory, quantity
                    );

                    inventory.setQuantityReserved(
                            Math.addExact(reserved, quantity)
                    );
                }

                case STOCK_RELEASED -> {
                    if (reserved < quantity) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Cannot release more stock than is reserved."
                        );
                    }

                    inventory.setQuantityReserved(reserved - quantity);
                }

                case STOCK_SOLD -> {
                    if (reserved < quantity) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Insufficient reserved stock to confirm this sale."
                        );
                    }

                    inventory.setQuantityOnHand(onHand - quantity);
                    inventory.setQuantityReserved(reserved - quantity);
                }

                case STOCK_ADJUSTED -> {
                    int adjustedOnHand = Math.addExact(onHand, quantity);

                    if (adjustedOnHand < 0) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Physical stock cannot be negative."
                        );
                    }

                    if (adjustedOnHand < reserved) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Stock cannot be reduced below reserved quantity."
                        );
                    }

                    inventory.setQuantityOnHand(adjustedOnHand);
                }
            }
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock quantity exceeds the supported range."
            );
        }

        inventoryRepository.save(inventory);
        recordInventoryMovement(inventory, type, quantity, reason);
    }

    private void validateMovementQuantity(
            Integer quantity,
            InventoryMovementType type
    ) {
        if (type == InventoryMovementType.STOCK_ADJUSTED) {
            if (quantity == null || quantity == 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stock adjustment must be a non-zero quantity."
                );
            }
        } else {
            productValidationService.validateQuantity(quantity);
        }
    }

    private void recordInventoryMovement(
            Inventory inventory,
            InventoryMovementType type,
            Integer quantity,
            String reason
    ) {
        InventoryMovement movement = new InventoryMovement();
        movement.setInventory(inventory);
        movement.setType(type);
        movement.setQuantity(quantity);
        movement.setReason(reason);

        inventoryMovementRepository.save(movement);
    }
}