package com.pehlione.kafka.api;

import com.pehlione.kafka.config.KafkaTopicProperties;
import com.pehlione.kafka.messaging.KafkaMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final KafkaMessageProducer kafkaMessageProducer;
    private final KafkaTopicProperties kafkaTopicProperties;

    @PostMapping
    public String sendPost(@RequestParam String message,
                           @RequestParam(required = false) String topic) {
        String targetTopic = topic == null || topic.isBlank()
                ? kafkaTopicProperties.getTopics().getAuditEvent().getName()
                : topic;
        kafkaMessageProducer.send(targetTopic, message);
        return "Mesaj Kafka'ya gonderildi. topic=" + targetTopic + ", payload=" + message;
    }
}
