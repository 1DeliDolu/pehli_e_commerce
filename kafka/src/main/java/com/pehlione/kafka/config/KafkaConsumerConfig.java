package com.pehlione.kafka.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties kafkaProperties,
                                                           KafkaTopicProperties topicProperties) {
        KafkaTopicProperties.Consumer settings = topicProperties.getConsumer();
        Map<String, Object> config = new HashMap<>(kafkaProperties.buildConsumerProperties());

        config.putIfAbsent(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.putIfAbsent(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        put(config, ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, settings.getAutoOffsetReset());
        put(config, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, settings.getEnableAutoCommit());
        put(config, ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, settings.getAutoCommitIntervalMs());
        put(config, ConsumerConfig.MAX_POLL_RECORDS_CONFIG, settings.getMaxPollRecords());
        put(config, ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, settings.getMaxPollIntervalMs());
        put(config, ConsumerConfig.FETCH_MIN_BYTES_CONFIG, settings.getFetchMinBytes());
        put(config, ConsumerConfig.FETCH_MAX_BYTES_CONFIG, settings.getFetchMaxBytes());
        put(config, ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, settings.getMaxPartitionFetchBytes());
        put(config, ConsumerConfig.ISOLATION_LEVEL_CONFIG, settings.getIsolationLevel());
        put(config, ConsumerConfig.GROUP_PROTOCOL_CONFIG, settings.getGroupProtocol());
        put(config, ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, settings.getPartitionAssignmentStrategy());
        put(config, CommonClientConfigs.CLIENT_DNS_LOOKUP_CONFIG, settings.getClientDnsLookup());

        if ("classic".equalsIgnoreCase(settings.getGroupProtocol())) {
            put(config, ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, settings.getHeartbeatIntervalMs());
            put(config, ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, settings.getSessionTimeoutMs());
        }

        if (settings.getGroupInstanceId() != null && !settings.getGroupInstanceId().isBlank()) {
            put(config, ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, settings.getGroupInstanceId());
        }

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    private void put(Map<String, Object> config, String key, Object value) {
        if (value != null) {
            config.put(key, value);
        }
    }
}
