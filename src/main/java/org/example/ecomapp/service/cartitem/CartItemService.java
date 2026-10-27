package org.example.ecomapp.service.cartitem;

import lombok.RequiredArgsConstructor;
import org.example.ecomapp.dto.cartitem.CartItemRequest;
import org.example.ecomapp.dto.cartitem.CartItemResponse;
import org.example.ecomapp.model.CartItem;
import org.example.ecomapp.model.Product;
import org.example.ecomapp.model.User;
import org.example.ecomapp.repository.CartItemRepository;
import org.example.ecomapp.repository.ProductRepository;
import org.example.ecomapp.repository.UserRepository;
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


    private void validateProductAvailability(Product product){
        if(!Boolean.TRUE.equals(product.getActive())){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    product.getName() + " is currently unavailable."
            );
        }

        if(product.getQuantity() ==null|| product.getQuantity()<=0){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    product.getName() + " is out of stock."
            );
        }
        if(product.getPrice()==null || product.getPrice().signum()<0){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    product.getName() + " has invalid price."
            );
        }

    }
    private void validateStock(Product product, int requestedQuantity){
        if(requestedQuantity> product.getQuantity()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only " + product.getQuantity() + " units of " + product.getName() + " are currently available"
            );
        }
    }

//    private void validateQuantity(Integer quantity) {
//        if (quantity == null || quantity <= 0) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    quantity + " is not a valid  quantity."
//            );
//        }
//    }

    private CartItemResponse toCartResponse(CartItem cartItem){
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
    public CartItemResponse addToCart (Long userId, CartItemRequest request){

        //validateQuantity(request.getQuantity());
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResponseStatusException(
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

        validateProductAvailability(product);

        CartItem cartItem = cartItemRepository
                .findByUser_IdAndProduct_Id(userId, product.getId())
                .orElse(null);

        int newQuantity = request.getQuantity();
        if(cartItem!= null){
            newQuantity += cartItem.getQuantity();
        }else{
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
        }
        validateStock(product, newQuantity);
        cartItem.setQuantity(newQuantity);
        cartItem.setAmount(
                product.getPrice().multiply(
                        BigDecimal.valueOf(newQuantity)
                )
        );

        return toCartResponse(cartItemRepository.save(cartItem));
    }
    @Transactional(readOnly = true)
    public List<CartItemResponse> fetchCartItems(Long userId){
        if(!userRepository.existsById(userId)){
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
    public CartItemResponse updateCartItem(Long userId,Long cartItemId,CartItemRequest request){
        //validateQuantity(request.getQuantity());
        CartItem cartItem = cartItemRepository.findByIdAndUser_Id(cartItemId,userId)
                .orElseThrow(
                        ()->new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Cart item with ID " + cartItemId
                                        + " was not found for this user"
                        )
                );
        Product product = cartItem.getProduct();
        validateProductAvailability(product);
        validateStock(product,request.getQuantity());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setAmount(
                product.getPrice().multiply(
                        BigDecimal.valueOf(request.getQuantity())
                )
        );
        return toCartResponse(cartItemRepository.save(cartItem));
    }
    @Transactional
    public String removeCartItem(Long userId,Long cartItemId){
        CartItem cartItem = cartItemRepository
                .findByIdAndUser_Id(cartItemId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item with ID " + cartItemId
                                + " was not found for this user."
                ));
        cartItemRepository.delete(cartItem);

        return cartItem.getProduct().getName() + " removed from your cart";

    }

}

