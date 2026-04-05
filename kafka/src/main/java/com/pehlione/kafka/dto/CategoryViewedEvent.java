package com.pehlione.kafka.dto;

import java.time.LocalDateTime;

public record CategoryViewedEvent(
        String eventType,
        String sessionId,
        Long categoryId,
        String categoryName,
        LocalDateTime occurredAt
) {
}
