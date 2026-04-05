package com.pehlione.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminProductEvent(
        String eventType,
        String actorUsername,
        Long productId,
        String productName,
        String categoryName,
        BigDecimal price,
        LocalDateTime occurredAt
) {
}
