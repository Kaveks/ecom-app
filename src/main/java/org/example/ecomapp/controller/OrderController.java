
package org.example.ecomapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.ecomapp.dto.order.OrderResponse;
import org.example.ecomapp.service.order.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @PathVariable Long userId
    ) {
        OrderResponse response = orderService.checkout(userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
