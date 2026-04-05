package com.pehlione.kafka.dto;

public record OrderDashboardSummary(
        long totalOrders,
        long placedOrders,
        long canceledOrders,
        long distinctCustomers,
        long repeatCustomers
) {
}
