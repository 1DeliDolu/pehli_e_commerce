package com.pehlione.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MailJobMessage(
        String jobId,
        String recipientName,
        String recipientEmail,
        String subject,
        String body,
        int itemCount,
        BigDecimal subtotal,
        String sourceEvent,
        int attempt,
        LocalDateTime createdAt
) {

    public MailJobMessage nextAttempt() {
        return new MailJobMessage(
                jobId,
                recipientName,
                recipientEmail,
                subject,
                body,
                itemCount,
                subtotal,
                sourceEvent,
                attempt + 1,
                createdAt
        );
    }
}
