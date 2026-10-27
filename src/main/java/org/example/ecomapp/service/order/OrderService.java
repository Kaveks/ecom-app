package org.example.ecomapp.service.order;

import lombok.RequiredArgsConstructor;
import org.example.ecomapp.dto.order.OrderItemResponse;
import org.example.ecomapp.dto.order.OrderResponse;
import org.example.ecomapp.model.*;
import org.example.ecomapp.repository.CartItemRepository;
import org.example.ecomapp.repository.OrderRepository;
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
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductValidationService productValidationService;

    @Transactional
    public OrderResponse checkout(Long userId){
        User user =userRepository.findById(userId)
                .orElseThrow(()-> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with ID " + userId + " was not found."
                )
        );
        List<CartItem> cartItems= cartItemRepository.findByUser_Id(userId);
        if(cartItems.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Your cart is empty."
            );
        }
        Order order= new Order();
        order.setUser(user);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for(CartItem cartItem:cartItems){
            Product product = productRepository.findById(
                    cartItem.getProduct().getId()
            ).orElseThrow(()-> new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A product in your cart is no longer available."
            ));
            productValidationService.validateQuantity(cartItem.getQuantity());

            productValidationService.validateProductAvailability(product);

            productValidationService.validateStock(product, cartItem.getQuantity());
            BigDecimal unitPrice = product.getPrice();
            BigDecimal totalPrice=unitPrice.multiply(
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
        };
        order.setTotalAmount(totalAmount);
        Order savedOrder= orderRepository.save(order);

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
