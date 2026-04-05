package com.pehlione.kafka.dto;

public record KafkaTopicConfigView(
        String cleanupPolicy,
        String retentionMs,
        String segmentBytes,
        String compressionType,
        String minInSyncReplicas,
        String maxMessageBytes,
        String messageTimestampType
) {
}
