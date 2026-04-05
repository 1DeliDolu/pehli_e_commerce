package com.pehlione.kafka.messaging;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaMessageConsumer {

    @KafkaListener(
            topics = {
                "${app.kafka.topics.user-login.name}",
                "${app.kafka.topics.user-click.name}"
            },
            groupId = "${app.kafka.consumers.activity.group-id:${app.kafka.consumer-group-id:activity-group}}",
            clientIdPrefix = "${app.kafka.consumers.activity.client-id:activity-consumer}",
            concurrency = "${app.kafka.consumers.activity.concurrency:1}"
    )
    public void consumeActivityEvent(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            String message) {
        logConsumedMessage("activity", topic, key, partition, offset, message);
    }

    @KafkaListener(
            topics = {
                "${app.kafka.topics.order-created.name}",
                "${app.kafka.topics.order-cancelled.name}"
            },
            groupId = "${app.kafka.consumers.order.group-id:${app.kafka.consumer-group-id:order-group}}",
            clientIdPrefix = "${app.kafka.consumers.order.client-id:order-consumer}",
            concurrency = "${app.kafka.consumers.order.concurrency:1}"
    )
    public void consumeOrderEvent(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            String message) {
        logConsumedMessage("order", topic, key, partition, offset, message);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.mail-requested.name}",
            groupId = "${app.kafka.consumers.mail-request.group-id:${app.kafka.consumer-group-id:mail-request-group}}",
            clientIdPrefix = "${app.kafka.consumers.mail-request.client-id:mail-request-consumer}",
            concurrency = "${app.kafka.consumers.mail-request.concurrency:1}"
    )
    public void consumeMailRequestEvent(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            String message) {
        logConsumedMessage("mail-request", topic, key, partition, offset, message);
    }

    @KafkaListener(
            topics = "${app.kafka.topics.audit-event.name}",
            groupId = "${app.kafka.consumers.audit.group-id:${app.kafka.consumer-group-id:audit-group}}",
            clientIdPrefix = "${app.kafka.consumers.audit.client-id:audit-consumer}",
            concurrency = "${app.kafka.consumers.audit.concurrency:1}"
    )
    public void consumeAuditEvent(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            String message) {
        logConsumedMessage("audit", topic, key, partition, offset, message);
    }

    private void logConsumedMessage(String listener,
            String topic,
            String key,
            int partition,
            long offset,
            String message) {
        // Optionally implement fallback or retry logic here
        log.info("The {} listener consumed a message. listener={}, topic={}, partition={}, offset={}, key={}, payload={}",
                listener,
                topic,
                partition,
                offset,
                key,
                message);
    }
}
