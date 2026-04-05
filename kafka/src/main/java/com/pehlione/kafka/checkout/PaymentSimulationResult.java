package com.pehlione.kafka.checkout;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentSimulationResult(
        CheckoutPaymentMethod paymentMethod,
        String status,
        String transactionId,
        String providerReference,
        String providerLabel,
        String message,
        BigDecimal amount,
        LocalDateTime approvedAt,
        boolean simulated
) {
}
