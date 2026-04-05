package com.pehlione.kafka.dto;

import java.time.LocalDateTime;

public record CustomerOrderAnalyticsView(
    String customerName,
    String customerEmail,
    long totalOrders,
    long canceledOrders,
    String lastCanceledOrderNumber,
    LocalDateTime lastOrderAt,
    Double averageOrderIntervalDays
) {

    public String purchaseFrequencyLabel() {
    if (averageOrderIntervalDays == null) {
        return "First order / insufficient data";
    }
    return String.format(java.util.Locale.US, "Every %.1f days", averageOrderIntervalDays);
    }
}
