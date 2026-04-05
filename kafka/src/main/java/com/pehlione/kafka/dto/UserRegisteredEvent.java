package com.pehlione.kafka.dto;

import java.time.LocalDateTime;

public record UserRegisteredEvent(
        String eventType,
        String username,
        String email,
        String role,
        LocalDateTime occurredAt
) {
}
