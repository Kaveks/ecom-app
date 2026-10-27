package org.example.ecomapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecomapp.dto.cartitem.CartItemRequest;
import org.example.ecomapp.dto.cartitem.CartItemResponse;
import org.example.ecomapp.service.cartitem.CartItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @GetMapping("/{userId}/items")
    public ResponseEntity<List<CartItemResponse>> fetchCartItems(@PathVariable Long userId){
        return ResponseEntity.ok(cartItemService.fetchCartItems(userId));
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartItemResponse> addToCart(
            @PathVariable Long userId,
            @Valid @RequestBody CartItemRequest request){
        CartItemResponse response = cartItemService.addToCart(userId,request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateCartItem(
            @PathVariable Long userId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemRequest request
    ){
        return ResponseEntity.ok(
                cartItemService.updateCartItem(userId,cartItemId,request)
        );

    }

    @DeleteMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<String> removeCartItem(
            @PathVariable Long userId,
            @PathVariable Long cartItemId
    ) {
        String message = cartItemService.removeCartItem(userId, cartItemId);

        return ResponseEntity.ok(message);
    }


}
