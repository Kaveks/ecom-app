package org.example.ecomapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ecomapp.dto.payment.PaymentRequest;
import org.example.ecomapp.service.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    //update payment reference
    @PostMapping("/{orderId}/callback")
    public ResponseEntity<String> handlePaymentCallback(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentRequest request
    ) {
        paymentService.processPaymentCallback(orderId, request);
        return ResponseEntity.ok("Payment callback processed successfully.");
    }
}