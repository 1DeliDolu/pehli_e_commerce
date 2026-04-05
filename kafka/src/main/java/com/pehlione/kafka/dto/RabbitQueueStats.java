package com.pehlione.kafka.dto;

public record RabbitQueueStats(
        String name,
        int ready,
        int unacked,
        int total,
        int consumers,
        double incomingRate,
        double outgoingRate,
        String state
) {
}
