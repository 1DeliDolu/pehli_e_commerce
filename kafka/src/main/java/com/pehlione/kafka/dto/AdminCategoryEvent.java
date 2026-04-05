package com.pehlione.kafka.dto;

import java.time.LocalDateTime;

public record AdminCategoryEvent(
        String eventType,
        String actorUsername,
        Long categoryId,
        String categoryName,
        LocalDateTime occurredAt
) {
}
