package com.pehlione.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductViewedEvent(
        String eventType,
        String sessionId,
        Long productId,
        String productName,
        String categoryName,
        BigDecimal price,
        LocalDateTime occurredAt
) {
}
