package com.pehlione.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaTopicProperties {

    private Admin admin = new Admin();
    private String consumerGroupId;
    private Consumer consumer = new Consumer();
    private Consumers consumers = new Consumers();
    private Topics topics = new Topics();

    @Getter
    @Setter
    public static class Admin {
        private String bootstrapServers;
        private String bootstrapControllers;
        private String clientId = "topic-admin";
        private Integer defaultApiTimeoutMs = 60000;
        private Integer requestTimeoutMs = 30000;
        private Integer connectionsMaxIdleMs = 300000;
        private String clientDnsLookup = "use_all_dns_ips";
        private Integer receiveBufferBytes = 65536;
    }

    @Getter
    @Setter
    public static class Consumer {
        private String autoOffsetReset = "earliest";
        private Boolean enableAutoCommit = true;
        private Integer autoCommitIntervalMs = 5000;
        private Integer maxPollRecords = 100;
        private Integer maxPollIntervalMs = 300000;
        private Integer fetchMinBytes = 1;
        private Integer fetchMaxBytes = 52428800;
        private Integer maxPartitionFetchBytes = 1048576;
        private Integer heartbeatIntervalMs = 3000;
        private Integer sessionTimeoutMs = 45000;
        private String isolationLevel = "read_uncommitted";
        private String groupProtocol = "classic";
        private String partitionAssignmentStrategy =
                "org.apache.kafka.clients.consumer.RangeAssignor,"
                        + "org.apache.kafka.clients.consumer.CooperativeStickyAssignor";
        private String clientDnsLookup = "use_all_dns_ips";
        private String groupInstanceId;
    }

    @Getter
    @Setter
    public static class Consumers {
        private ListenerDefinition activity = new ListenerDefinition();
        private ListenerDefinition order = new ListenerDefinition();
        private ListenerDefinition mailRequest = new ListenerDefinition();
        private ListenerDefinition audit = new ListenerDefinition();
    }

    @Getter
    @Setter
    public static class Topics {
        private TopicDefinition userLogin = new TopicDefinition();
        private TopicDefinition userClick = new TopicDefinition();
        private TopicDefinition orderCreated = new TopicDefinition();
        private TopicDefinition orderCancelled = new TopicDefinition();
        private TopicDefinition mailRequested = new TopicDefinition();
        private TopicDefinition auditEvent = new TopicDefinition();
    }

    @Getter
    @Setter
    public static class ListenerDefinition {
        private String groupId;
        private String clientId;
        private Integer concurrency = 1;
    }

    @Getter
    @Setter
    public static class TopicDefinition {
        private String name;
        private int partitions = 1;
        private int replicas = 1;
        private String cleanupPolicy;
        private Long retentionMs;
        private Integer segmentBytes;
        private String compressionType;
        private Integer minInSyncReplicas;
        private Integer maxMessageBytes;
        private String messageTimestampType;
    }
}
