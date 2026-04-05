package com.pehlione.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaTopicConfig {

    private final KafkaTopicProperties topicProperties;

    public KafkaTopicConfig(KafkaTopicProperties topicProperties) {
        this.topicProperties = topicProperties;
    }

    @Bean
    public NewTopic userLoginTopic() {
        return buildTopic(topicProperties.getTopics().getUserLogin());
    }

    @Bean
    public NewTopic userClickTopic() {
        return buildTopic(topicProperties.getTopics().getUserClick());
    }

    @Bean
    public NewTopic orderCreatedTopic() {
        return buildTopic(topicProperties.getTopics().getOrderCreated());
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return buildTopic(topicProperties.getTopics().getOrderCancelled());
    }

    @Bean
    public NewTopic mailRequestedTopic() {
        return buildTopic(topicProperties.getTopics().getMailRequested());
    }

    @Bean
    public NewTopic auditEventTopic() {
        return buildTopic(topicProperties.getTopics().getAuditEvent());
    }

    private NewTopic buildTopic(KafkaTopicProperties.TopicDefinition definition) {
        TopicBuilder builder = TopicBuilder.name(definition.getName())
                .partitions(definition.getPartitions())
                .replicas(definition.getReplicas());

        applyConfig(builder, "cleanup.policy", definition.getCleanupPolicy());
        applyConfig(builder, "retention.ms", definition.getRetentionMs());
        applyConfig(builder, "segment.bytes", definition.getSegmentBytes());
        applyConfig(builder, "compression.type", definition.getCompressionType());
        applyConfig(builder, "min.insync.replicas", definition.getMinInSyncReplicas());
        applyConfig(builder, "max.message.bytes", definition.getMaxMessageBytes());
        applyConfig(builder, "message.timestamp.type", definition.getMessageTimestampType());

        return builder.build();
    }

    private void applyConfig(TopicBuilder builder, String key, Object value) {
        if (value != null) {
            builder.config(key, String.valueOf(value));
        }
    }
}
