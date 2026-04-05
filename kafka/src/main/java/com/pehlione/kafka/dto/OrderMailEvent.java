package com.pehlione.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderMailEvent(
        String eventType,
        String username,
        String recipientEmail,
        int itemCount,
        BigDecimal subtotal,
        LocalDateTime occurredAt
) {
}
