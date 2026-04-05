package com.pehlione.kafka.messaging;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pehlione.kafka.dto.MailJobMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailQueueProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbitmq.mail.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.mail.routing-key}")
    private String routingKey;

    @Value("${app.rabbitmq.mail.retry-routing-key}")
    private String retryRoutingKey;

    public void enqueue(MailJobMessage job) {
        send(job, routingKey, "mail.send");
    }

    public void enqueueRetry(MailJobMessage job) {
        send(job, retryRoutingKey, "mail.retry");
    }

    private void send(MailJobMessage job, String targetRoutingKey, String operation) {
        try {
            CorrelationData correlationData = new CorrelationData(job.jobId());
            rabbitTemplate.convertAndSend(exchange, targetRoutingKey, objectMapper.writeValueAsString(job), correlationData);
            log.info("The MailQueueProducer sent a mail job to the queue. operation={}, jobId={}, exchange={}, routingKey={}, attempt={}",
                    operation,
                    job.jobId(),
                    exchange,
                    targetRoutingKey,
                    job.attempt());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("The MailQueueProducer failed to convert mail job to JSON format.", exception);
        }
    }
}
