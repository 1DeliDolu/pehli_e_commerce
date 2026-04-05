package com.pehlione.kafka.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pehlione.kafka.dto.RabbitDlqMessageView;
import com.pehlione.kafka.dto.RabbitMailQueueOverview;
import com.pehlione.kafka.dto.RabbitQueueStats;
import com.rabbitmq.client.GetResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RabbitMqMonitoringService {

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Value("${app.rabbitmq.management.base-url}")
    private String managementBaseUrl;

    @Value("${app.rabbitmq.management.username}")
    private String username;

    @Value("${app.rabbitmq.management.password}")
    private String password;

    @Value("${app.rabbitmq.mail.queue}")
    private String mailQueueName;

    @Value("${app.rabbitmq.mail.dlq}")
    private String deadLetterQueueName;

    @Value("${app.rabbitmq.mail.retry-queue}")
    private String retryQueueName;

    @Value("${app.rabbitmq.mail.exchange}")
    private String mailExchange;

    @Value("${app.rabbitmq.mail.routing-key}")
    private String mailRoutingKey;

    @Value("${app.rabbitmq.mail.retry-batch-size}")
    private int retryBatchSize;

    public RabbitMailQueueOverview fetchMailQueueOverview() {
        try {
            RabbitQueueStats mailQueue = fetchQueue(mailQueueName);
            RabbitQueueStats retryQueue = fetchQueue(retryQueueName);
            RabbitQueueStats deadLetterQueue = fetchQueue(deadLetterQueueName);
            List<RabbitDlqMessageView> deadLetterMessages = fetchDlqMessages(deadLetterQueueName, 10);
            return new RabbitMailQueueOverview(
                    true,
                    "RabbitMQ queue overview fetched successfully.",
                    mailQueue,
                    retryQueue,
                    deadLetterQueue,
                    deadLetterMessages
            );
        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
            return unavailableOverview(exception);
        } catch (RuntimeException exception) {
            return unavailableOverview(exception);
        }
    }

    public int retryDeadLetterMessages() {
        return rabbitTemplate.execute(channel -> {
            int retried = 0;
            while (retried < retryBatchSize) {
                GetResponse response = channel.basicGet(deadLetterQueueName, false);
                if (response == null) {
                    break;
                }

                try {
                    channel.basicPublish(mailExchange, mailRoutingKey, response.getProps(), response.getBody());
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                    retried++;
                } catch (Exception exception) {
                    channel.basicNack(response.getEnvelope().getDeliveryTag(), false, true);
                    throw new IllegalStateException("DLQ message could not be re-queued.", exception);
                }
            }
            return retried;
        });
    }

    private RabbitMailQueueOverview unavailableOverview(Exception exception) {
        return new RabbitMailQueueOverview(
                false,
                "RabbitMQ Management API is unavailable: " + exception.getMessage(),
                emptyStats(mailQueueName),
                emptyStats(retryQueueName),
                emptyStats(deadLetterQueueName),
                List.of()
        );
    }

    private RabbitQueueStats fetchQueue(String queueName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(queueUri(queueName))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", basicAuthHeader())
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("Queue information could not be retrieved. queue=%s status=%s".formatted(queueName, response.statusCode()));
        }

        JsonNode node = objectMapper.readTree(response.body());
        JsonNode messageStats = node.path("message_stats");
        return new RabbitQueueStats(
                node.path("name").asText(queueName),
                node.path("messages_ready").asInt(0),
                node.path("messages_unacknowledged").asInt(0),
                node.path("messages").asInt(0),
                node.path("consumers").asInt(0),
                messageStats.path("publish_details").path("rate").asDouble(0.0),
                messageStats.path("deliver_get_details").path("rate").asDouble(0.0),
                node.path("state").asText("unknown")
        );
    }

    private List<RabbitDlqMessageView> fetchDlqMessages(String queueName, int count) throws IOException, InterruptedException {
        String body = """
                {
                  "count": %d,
                  "ackmode": "ack_requeue_true",
                  "encoding": "auto",
                  "truncate": 5000
                }
                """.formatted(count);

        HttpRequest request = HttpRequest.newBuilder(queueGetUri(queueName))
                .timeout(Duration.ofSeconds(5))
                .header("Authorization", basicAuthHeader())
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400) {
            throw new IllegalStateException("DLQ messages could not be retrieved. queue=%s status=%s".formatted(queueName, response.statusCode()));
        }

        JsonNode root = objectMapper.readTree(response.body());
        List<RabbitDlqMessageView> messages = new ArrayList<>();
        for (JsonNode message : root) {
            messages.add(new RabbitDlqMessageView(
                    message.path("exchange").asText(""),
                    message.path("routing_key").asText(""),
                    message.path("redelivered").asBoolean(false),
                    message.path("payload").asText("")
            ));
        }
        return messages;
    }

    private RabbitQueueStats emptyStats(String queueName) {
        return new RabbitQueueStats(queueName, 0, 0, 0, 0, 0.0, 0.0, "unknown");
    }

    private URI queueUri(String queueName) {
        return URI.create(managementBaseUrl + "/queues/%2F/" + queueName);
    }

    private URI queueGetUri(String queueName) {
        return URI.create(managementBaseUrl + "/queues/%2F/" + queueName + "/get");
    }

    private String basicAuthHeader() {
        String raw = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
