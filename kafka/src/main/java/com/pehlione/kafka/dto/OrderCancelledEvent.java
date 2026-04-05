package com.pehlione.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCancelledEvent(
        String eventType,
        Long orderId,
        String orderNumber,
        String sessionId,
        String customerEmail,
        String customerName,
        BigDecimal subtotal,
        String cancelReason,
        LocalDateTime canceledAt
) {
}
