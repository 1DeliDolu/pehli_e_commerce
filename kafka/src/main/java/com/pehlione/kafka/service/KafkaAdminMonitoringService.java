package com.pehlione.kafka.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.stereotype.Service;

import com.pehlione.kafka.config.KafkaTopicProperties;
import com.pehlione.kafka.dto.KafkaAdminOverview;
import com.pehlione.kafka.dto.KafkaTopicConfigView;
import com.pehlione.kafka.dto.KafkaTopicMetadataView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaAdminMonitoringService {

    private final AdminClient adminClient;
    private final KafkaTopicProperties topicProperties;

    public KafkaAdminOverview fetchOverview() {
        List<String> topicNames = List.of(
                topicProperties.getTopics().getUserLogin().getName(),
                topicProperties.getTopics().getUserClick().getName(),
                topicProperties.getTopics().getOrderCreated().getName(),
                topicProperties.getTopics().getMailRequested().getName(),
                topicProperties.getTopics().getAuditEvent().getName()
        );

        try {
            Map<String, TopicDescription> descriptions = describeTopics(topicNames);
            Map<ConfigResource, Config> configs = describeTopicConfigs(topicNames);

            List<KafkaTopicMetadataView> topics = topicNames.stream()
                    .map(topic -> toTopicView(descriptions.get(topic), configs.get(topicResource(topic))))
                    .sorted(Comparator.comparing(KafkaTopicMetadataView::topicName))
                    .toList();

            return new KafkaAdminOverview(
                    true,
                    "The KafkaAdminMonitoringService fetched Kafka topic metadata successfully.",
                    topicProperties.getAdmin().getBootstrapServers(),
                    topicProperties.getAdmin().getClientId(),
                    topics.size(),
                    topics
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return unavailable("The KafkaAdminMonitoringService failed to fetch Kafka topic metadata. The operation was interrupted.");
        } catch (ExecutionException exception) {
            return unavailable("The KafkaAdminMonitoringService failed to fetch Kafka topic metadata: " + rootMessage(exception));
        } catch (RuntimeException exception) {
            return unavailable("The KafkaAdminMonitoringService failed to fetch Kafka topic metadata: " + rootMessage(exception));
        }
    }

    private Map<String, TopicDescription> describeTopics(List<String> topicNames)
            throws ExecutionException, InterruptedException {
        DescribeTopicsResult result = adminClient.describeTopics(topicNames);
        Map<String, KafkaFuture<TopicDescription>> futures = result.topicNameValues();
        Map<String, TopicDescription> descriptions = new LinkedHashMap<>();
        for (String topicName : topicNames) {
            descriptions.put(topicName, futures.get(topicName).get());
        }
        return descriptions;
    }

    private Map<ConfigResource, Config> describeTopicConfigs(List<String> topicNames)
            throws ExecutionException, InterruptedException {
        List<ConfigResource> resources = topicNames.stream()
                .map(topicName -> this.topicResource(topicName))
                .toList();

        DescribeConfigsResult result = adminClient.describeConfigs(resources);
        Map<ConfigResource, Config> configs = new LinkedHashMap<>();
        for (ConfigResource resource : resources) {
            configs.put(resource, result.values().get(resource).get());
        }
        return configs;
    }

    private KafkaTopicMetadataView toTopicView(TopicDescription description, Config config) {
        short replicationFactor = description.partitions().isEmpty()
                ? 0
                : (short) description.partitions().getFirst().replicas().size();

        String leaders = description.partitions().stream()
                .map(partition -> "p" + partition.partition() + "->" + partition.leader().id())
                .reduce((left, right) -> left + ", " + right)
                .orElse("-");

        return new KafkaTopicMetadataView(
                description.name(),
                description.partitions().size(),
                replicationFactor,
                leaders,
                new KafkaTopicConfigView(
                        configValue(config, "cleanup.policy"),
                        configValue(config, "retention.ms"),
                        configValue(config, "segment.bytes"),
                        configValue(config, "compression.type"),
                        configValue(config, "min.insync.replicas"),
                        configValue(config, "max.message.bytes"),
                        configValue(config, "message.timestamp.type")
                )
        );
    }

    private String configValue(Config config, String key) {
        if (config == null || config.get(key) == null || config.get(key).value() == null) {
            return "-";
        }
        return config.get(key).value();
    }

    private ConfigResource topicResource(String topicName) {
        return new ConfigResource(ConfigResource.Type.TOPIC, topicName);
    }

    private KafkaAdminOverview unavailable(String message) {
        return new KafkaAdminOverview(
                false,
                message,
                topicProperties.getAdmin().getBootstrapServers(),
                topicProperties.getAdmin().getClientId(),
                0,
                List.of()
        );
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}
