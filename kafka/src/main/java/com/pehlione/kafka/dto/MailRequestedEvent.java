package com.pehlione.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MailRequestedEvent(
        String eventType,
        String jobId,
        String username,
        String recipientEmail,
        int itemCount,
        BigDecimal subtotal,
        LocalDateTime occurredAt
) {
}
