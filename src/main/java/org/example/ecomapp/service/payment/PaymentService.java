package org.example.ecomapp.service.payment;

import lombok.RequiredArgsConstructor;
import org.example.ecomapp.dto.payment.PaymentRequest;
import org.example.ecomapp.enums.InventoryMovementType;
import org.example.ecomapp.enums.OrderStatus;
import org.example.ecomapp.enums.PaymentStatus;
import org.example.ecomapp.model.Order;
import org.example.ecomapp.model.OrderItem;
import org.example.ecomapp.model.Payment;
import org.example.ecomapp.repository.OrderRepository;
import org.example.ecomapp.repository.PaymentRepository;
import org.example.ecomapp.service.inventory.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Transactional
    public void processPaymentCallback(Long orderId, PaymentRequest request){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order with ID " + orderId + " was not found."
                ));

        // Prevent duplicate processing / idempotent check
        if (order.getStatus() == OrderStatus.PAID) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order is already paid."
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot process payment for a cancelled/failed order."
            );
        }
        // Check if transaction reference is already used
        paymentRepository.findByTransactionReference(request.getTransactionReference())
                .ifPresent(existingPayment -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Transaction reference already processed."
                    );
                });
        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseGet(() -> {
                    Payment newPayment = new Payment();
                    newPayment.setOrder(order);
                    newPayment.setAmount(order.getTotalAmount());
                    return newPayment;
                });

        payment.setTransactionReference(request.getTransactionReference());
        payment.setStatus(request.getStatus());
        paymentRepository.save(payment);
        if (request.getStatus() == PaymentStatus.SUCCESS) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);

            // Convert reserved stock into sold stock
            for (OrderItem item : order.getItems()) {
                inventoryService.processStock(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        InventoryMovementType.STOCK_SOLD,
                        "Payment confirmed for order ID " + order.getId()
                );
            }
        } else if (request.getStatus() == PaymentStatus.FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            // Release reserved stock back into available stock
            for (OrderItem item : order.getItems()) {
                inventoryService.processStock(
                        item.getProduct().getId(),
                        item.getQuantity(),
                        InventoryMovementType.STOCK_RELEASED,
                        "Payment failed for order ID " + order.getId()
                );
            }
        }
    }
}