package org.example.ecomapp.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.ecomapp.enums.PaymentStatus;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Payment status is required.")
    private PaymentStatus status;

    @NotBlank(message = "Transaction reference is required.")
    private String transactionReference;
}