package com.pehlione.kafka.dto;

import java.util.List;

public record KafkaAdminOverview(
        boolean available,
        String statusMessage,
        String bootstrapServers,
        String clientId,
        int topicCount,
        List<KafkaTopicMetadataView> topics
) {
}
