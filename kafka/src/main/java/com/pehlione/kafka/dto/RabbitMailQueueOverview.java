package com.pehlione.kafka.dto;

import java.util.List;

public record RabbitMailQueueOverview(
        boolean available,
        String statusMessage,
        RabbitQueueStats mailQueue,
        RabbitQueueStats retryQueue,
        RabbitQueueStats deadLetterQueue,
        List<RabbitDlqMessageView> deadLetterMessages
) {
}
