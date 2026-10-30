
package org.example.ecomapp.service.order;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.example.ecomapp.dto.order.OrderItemResponse;
import org.example.ecomapp.dto.order.OrderResponse;
import org.example.ecomapp.enums.InventoryMovementType;
import org.example.ecomapp.enums.OrderStatus;
import org.example.ecomapp.model.*;
import org.example.ecomapp.repository.CartItemRepository;
import org.example.ecomapp.repository.InventoryRepository;
import org.example.ecomapp.repository.OrderRepository;
import org.example.ecomapp.repository.ProductRepository;
import org.example.ecomapp.repository.UserRepository;
import org.example.ecomapp.service.inventory.InventoryService;
import org.example.ecomapp.service.product.ProductValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
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

    //retrieve order
    @Transactional(readOnly = true)
    public OrderResponse fetchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order with ID " + orderId + " was not found."
                ));
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> fetchUserOrders(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User with ID " + userId + " was not found."
            );
        }

        return orderRepository.findByUser_Id(userId)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }
    //cancel order@Transactional
    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order with ID " + orderId + " was not found."
                ));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order is already cancelled."
            );
        }

        if (order.getStatus() == OrderStatus.PAID ||
                order.getStatus() == OrderStatus.PROCESSING ||
                order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot cancel an order that has already been paid or fulfilled."
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Release reserved stock back to inventory
        for (OrderItem item : order.getItems()) {
            inventoryService.processStock(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    InventoryMovementType.STOCK_RELEASED,
                    reason
            );
        }
    }
    @Value("${app.order.reservation-expiry-minutes:15}")
    private long reservationExpiryMinutes;

//    @Scheduled(fixedRate = 60000) // Runs every 60 seconds in the background
//    @Transactional
//    public void expireOldPendingOrders() {
//        LocalDateTime expiryThreshold = LocalDateTime.now().minusMinutes(reservationExpiryMinutes);
//
//        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(
//                OrderStatus.PENDING,
//                expiryThreshold
//        );
//
//        if (!expiredOrders.isEmpty()) {
//            log.info("Found {} expired PENDING order(s) to automatically cancel.", expiredOrders.size());
//        }
//
//        for (Order order : expiredOrders) {
//            try {
//                // Re-verify status under transaction to prevent race conditions
//                // with a simultaneous payment callback
//                if (order.getStatus() == OrderStatus.PENDING) {
//                    log.info("Automatically cancelling expired order ID: {} created at: {}", order.getId(), order.getCreatedAt());
//                    cancelOrder(order.getId(), "Released for expired reservation order ID " + order.getId());
//                }
//            } catch (Exception e) {
//                log.error("Failed to automatically expire order ID: {}", order.getId(), e);
//            }
//        }
//    }
@Transactional
public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
    Order order = findOrderById(orderId);
    OrderStatus currentStatus = order.getStatus();

    validateTransition(currentStatus, newStatus);

    order.setStatus(newStatus);
    Order updatedOrder = orderRepository.save(order);

    log.info("Order ID: {} transitioned from {} to {}", orderId, currentStatus, newStatus);
    return toOrderResponse(updatedOrder);
}

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order with ID " + orderId + " was not found."
                ));
    }

    private void validateTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot update status of a cancelled order."
            );
        }

        boolean isValidTransition = switch (currentStatus) {
            case PAID -> newStatus == OrderStatus.PROCESSING;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            default -> false;
        };

        if (!isValidTransition) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invalid state transition from " + currentStatus + " to " + newStatus
            );
        }
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
