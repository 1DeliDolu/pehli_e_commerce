package com.pehlione.kafka.dto;

public record RabbitDlqMessageView(
        String exchange,
        String routingKey,
        boolean redelivered,
        String payload
) {
}
