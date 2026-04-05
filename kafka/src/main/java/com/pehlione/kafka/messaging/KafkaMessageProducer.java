package com.pehlione.kafka.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Async
    public void send(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.warn("The Kafka producer failed to send a message. topic={}, key={}", topic, key, exception);
                        return;
                    }
                    log.info("The Kafka producer sent a message successfully. topic={}, partition={}, offset={}, key={}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset(),
                            key);
                });
    }

    @Async
    public void send(String topic, String message) {
        send(topic, null, message);
    }
}
