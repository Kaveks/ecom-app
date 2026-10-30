
package org.example.ecomapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.ecomapp.dto.order.OrderResponse;
import org.example.ecomapp.enums.OrderStatus;
import org.example.ecomapp.service.order.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/user/{userId}/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @PathVariable Long userId
    ) {
        OrderResponse response = orderService.checkout(userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> fetchOrder(
            @PathVariable Long orderId
    ) {
        OrderResponse response = orderService.fetchOrder(orderId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> fetchUserOrders(
            @PathVariable Long userId
    ) {
        List<OrderResponse> responses = orderService.fetchUserOrders(userId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(
            @PathVariable Long orderId
    ) {
        orderService.cancelOrder(orderId, "Manually cancelled for order ID " + orderId);
        return ResponseEntity.ok("Order cancelled and reserved stock released successfully.");
    }
    //fulfilment transition
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    ) {
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(response);
    }
}
