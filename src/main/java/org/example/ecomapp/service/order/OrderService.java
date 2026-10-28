
package org.example.ecomapp.service.order;

import lombok.RequiredArgsConstructor;
import org.example.ecomapp.dto.order.OrderItemResponse;
import org.example.ecomapp.dto.order.OrderResponse;
import org.example.ecomapp.enums.InventoryMovementType;
import org.example.ecomapp.model.*;
import org.example.ecomapp.repository.CartItemRepository;
import org.example.ecomapp.repository.InventoryRepository;
import org.example.ecomapp.repository.OrderRepository;
import org.example.ecomapp.repository.ProductRepository;
import org.example.ecomapp.repository.UserRepository;
import org.example.ecomapp.service.inventory.InventoryService;
import org.example.ecomapp.service.product.ProductValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;
    private final ProductValidationService productValidationService;

    @Transactional
    public OrderResponse checkout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " was not found."
                ));

        List<CartItem> cartItems = cartItemRepository.findByUser_Id(userId);

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Your cart is empty."
            );
        }
        // Sort by product ID so concurrent checkouts acquire locks
        // in a consistent order.
        List<CartItem> sortedCartItems = cartItems.stream()
                .sorted(Comparator.comparing(
                        item -> item.getProduct().getId()
                ))
                .toList();

        // Load products and acquire inventory locks in product-ID order.
        Map<Long, Product> productsById = sortedCartItems.stream()
                .map(item -> productRepository.findById(
                        item.getProduct().getId()
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A product in your cart is no longer available."
                )))
                .collect(Collectors.toMap(
                        Product::getId,
                        Function.identity()
                ));

        Map<Long, Inventory> inventoryByProductId =
                sortedCartItems.stream()
                        .map(item -> {
                            Long productId =
                                    item.getProduct().getId();

                            return inventoryRepository
                                    .findByProductIdForUpdate(productId)
                                    .orElseThrow(
                                            () -> new ResponseStatusException(
                                                    HttpStatus.CONFLICT,
                                                    "Inventory is not configured for product "
                                                            + productsById
                                                            .get(productId)
                                                            .getName()
                                                            + "."
                                            )
                                    );
                        })
                        .collect(Collectors.toMap(
                                inventory -> inventory.getProduct().getId(),
                                Function.identity()
                        ));
        Order order = new Order();
        order.setUser(user);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Validate all cart lines and construct the order before
        // changing inventory quantities.
        for (CartItem cartItem : sortedCartItems) {
            Long productId = cartItem.getProduct().getId();

            // sorted product and inventory
            Product product = productsById.get(productId);
            Inventory inventory = inventoryByProductId.get(productId);

            productValidationService.validateQuantity(
                    cartItem.getQuantity()
            );

            productValidationService.validateProductAvailability(product);

            productValidationService.validateStock(
                    inventory,
                    cartItem.getQuantity()
            );

            BigDecimal unitPrice = product.getPrice();

            BigDecimal totalPrice = unitPrice.multiply(
                    BigDecimal.valueOf(cartItem.getQuantity())
            );

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setAmount(totalPrice);

            order.addItem(orderItem);
            totalAmount = totalAmount.add(totalPrice);
        }

        order.setTotalAmount(totalAmount);

        // Persist the order and its items.
        Order savedOrder = orderRepository.saveAndFlush(order);

        // Reserve stock using the existing inventory service.
        // Each movement and inventory update joins this transaction.
        for (CartItem cartItem : sortedCartItems) {
            Long productId = cartItem.getProduct().getId();

            inventoryService.processStock(
                    productId,
                    cartItem.getQuantity(),
                    InventoryMovementType.STOCK_RESERVED,
                    "Reserved for order ID " + savedOrder.getId()
            );
        }

        // Clear the cart only after order creation and reservation.
        cartItemRepository.deleteByUser_Id(userId);
        return toOrderResponse(savedOrder);
    }

    private OrderResponse toOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();

        response.setId(String.valueOf(order.getId()));
        response.setUserId(String.valueOf(order.getUser().getId()));
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toOrderItemResponse)
                .toList();

        response.setItems(items);

        return response;
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();

        response.setProductId(
                String.valueOf(item.getProduct().getId())
        );
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setAmount(item.getAmount());

        return response;
    }
}
