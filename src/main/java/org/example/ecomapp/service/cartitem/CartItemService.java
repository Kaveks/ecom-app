
package org.example.ecomapp.service.cartitem;

import lombok.RequiredArgsConstructor;
import org.example.ecomapp.dto.cartitem.CartItemRequest;
import org.example.ecomapp.dto.cartitem.CartItemResponse;
import org.example.ecomapp.model.CartItem;
import org.example.ecomapp.model.Inventory;
import org.example.ecomapp.model.Product;
import org.example.ecomapp.model.User;
import org.example.ecomapp.repository.CartItemRepository;
import org.example.ecomapp.repository.InventoryRepository;
import org.example.ecomapp.repository.ProductRepository;
import org.example.ecomapp.repository.UserRepository;
import org.example.ecomapp.service.product.ProductValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductValidationService productValidationService;

    private CartItemResponse toCartResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        Product product = cartItem.getProduct();

        response.setId(String.valueOf(cartItem.getId()));
        response.setProductId(String.valueOf(product.getId()));
        response.setProductName(product.getName());
        response.setUnitPrice(product.getPrice());
        response.setQuantity(cartItem.getQuantity());
        response.setAmount(cartItem.getAmount());
        response.setImageUrl(product.getImageUrl());
        response.setCreatedAt(cartItem.getCreatedAt());
        response.setUpdatedAt(cartItem.getUpdatedAt());

        return response;
    }

    @Transactional
    public CartItemResponse addToCart(
            Long userId,
            CartItemRequest request
    ) {
        productValidationService.validateQuantity(request.getQuantity());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " was not found."
                ));

        Product product = productRepository.findById(
                request.getProductId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product with ID " + request.getProductId()
                        + " was not found."
        ));

        productValidationService.validateProductAvailability(product);

        Inventory inventory = fetchInventory(product.getId());

        CartItem cartItem = cartItemRepository
                .findByUser_IdAndProduct_Id(userId, product.getId())
                .orElse(null);

        int newQuantity = request.getQuantity();

        if (cartItem != null) {
            newQuantity = Math.addExact(
                    cartItem.getQuantity(),
                    request.getQuantity()
            );
        } else {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
        }

        productValidationService.validateStock(inventory, newQuantity);

        cartItem.setQuantity(newQuantity);
        cartItem.setAmount(
                product.getPrice().multiply(BigDecimal.valueOf(newQuantity))
        );

        return toCartResponse(cartItemRepository.save(cartItem));
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> fetchCartItems(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User with ID " + userId + " was not found."
            );
        }

        return cartItemRepository.findByUser_Id(userId)
                .stream()
                .map(this::toCartResponse)
                .toList();
    }

    @Transactional
    public CartItemResponse updateCartItem(
            Long userId,
            Long cartItemId,
            CartItemRequest request
    ) {
        productValidationService.validateQuantity(request.getQuantity());

        CartItem cartItem = cartItemRepository
                .findByIdAndUser_Id(cartItemId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item with ID " + cartItemId
                                + " was not found for this user."
                ));

        Product product = cartItem.getProduct();

        productValidationService.validateProductAvailability(product);

        Inventory inventory = fetchInventory(product.getId());

        productValidationService.validateStock(
                inventory,
                request.getQuantity()
        );

        cartItem.setQuantity(request.getQuantity());
        cartItem.setAmount(
                product.getPrice().multiply(
                        BigDecimal.valueOf(request.getQuantity())
                )
        );

        return toCartResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public String removeCartItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository
                .findByIdAndUser_Id(cartItemId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item with ID " + cartItemId
                                + " was not found for this user."
                ));

        String productName = cartItem.getProduct().getName();
        cartItemRepository.delete(cartItem);

        return productName + " removed from your cart";
    }

    private Inventory fetchInventory(Long productId) {
        return inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory was not found for product ID "
                                + productId + "."
                ));
    }
}
