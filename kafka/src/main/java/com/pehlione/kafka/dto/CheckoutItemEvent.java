package com.pehlione.kafka.dto;

import java.math.BigDecimal;

public record CheckoutItemEvent(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
