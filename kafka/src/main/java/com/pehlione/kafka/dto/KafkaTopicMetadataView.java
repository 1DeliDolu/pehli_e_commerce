package com.pehlione.kafka.dto;

public record KafkaTopicMetadataView(
        String topicName,
        int partitionCount,
        short replicationFactor,
        String partitionLeaders,
        KafkaTopicConfigView config
) {
}
