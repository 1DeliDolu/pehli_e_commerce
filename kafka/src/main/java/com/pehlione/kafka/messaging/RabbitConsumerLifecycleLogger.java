package com.pehlione.kafka.messaging;

import org.springframework.amqp.rabbit.listener.AsyncConsumerStoppedEvent;
import org.springframework.amqp.rabbit.listener.ListenerContainerConsumerFailedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RabbitConsumerLifecycleLogger {

    @EventListener
    public void handleConsumerFailed(ListenerContainerConsumerFailedEvent event) {
        log.warn("Rabbit consumer failed. reason={}, fatal={}", event.getReason(), event.isFatal(), event.getThrowable());
    }

    @EventListener
    public void handleConsumerStopped(AsyncConsumerStoppedEvent event) {
        log.warn("The RabbitConsumerLifecycleLogger detected that a Rabbit consumer stopped. consumer={}", event.getConsumer());
    }
}
