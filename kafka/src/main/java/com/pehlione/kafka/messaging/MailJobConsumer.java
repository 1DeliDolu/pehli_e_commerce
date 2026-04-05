package com.pehlione.kafka.messaging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pehlione.kafka.dto.MailJobMessage;
import com.pehlione.kafka.service.OrderMailService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailJobConsumer {

    private final OrderMailService orderMailService;
    private final ObjectMapper objectMapper;
    private final MailQueueProducer mailQueueProducer;

    @org.springframework.beans.factory.annotation.Value("${app.rabbitmq.mail.max-attempts}")
    private int maxAttempts;

    @RabbitListener(
            queues = "${app.rabbitmq.mail.queue}",
            containerFactory = "manualAckRabbitListenerContainerFactory"
    )
    public void consume(Message message,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            MailJobMessage job = objectMapper.readValue(message.getBody(), MailJobMessage.class);
            orderMailService.send(job);
            channel.basicAck(deliveryTag, false);
            log.info("The MailJobConsumer successfully processed a mail job. jobId={}, recipient={}", job.jobId(), job.recipientEmail());
        } catch (JsonProcessingException exception) {
            log.warn("The MailJobConsumer failed to parse a mail job. Sending to DLQ. payload={}",
                    new String(message.getBody(), StandardCharsets.UTF_8), exception);
            channel.basicNack(deliveryTag, false, false);
        } catch (RuntimeException exception) {
            MailJobMessage job = objectMapper.readValue(message.getBody(), MailJobMessage.class);
            if (job.attempt() + 1 < maxAttempts) {
                MailJobMessage retryJob = job.nextAttempt();
                mailQueueProducer.enqueueRetry(retryJob);
                channel.basicAck(deliveryTag, false);
                log.warn("The MailJobConsumer encountered a temporary error. Moving mail job to retry queue. jobId={}, nextAttempt={}",
                        retryJob.jobId(),
                        retryJob.attempt(), exception);
                return;
            }

            log.warn("The MailJobConsumer reached the maximum number of attempts. Sending mail job to DLQ. jobId={}, attempt={}",
                    job.jobId(),
                    job.attempt(),
                    exception);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
