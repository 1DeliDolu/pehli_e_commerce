package com.pehlione.kafka.dto;

import java.time.LocalDateTime;

public record UserLoginEvent(
        String eventType,
        String username,
        String role,
        String status,
        LocalDateTime occurredAt
) {
}
