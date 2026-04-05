package com.pehlione.kafka.dto;

import java.time.LocalDateTime;

public record CartEvent(
        String eventType,
        String sessionId,
        Long productId,
        String productName,
        int quantity,
        LocalDateTime occurredAt
) {
}
