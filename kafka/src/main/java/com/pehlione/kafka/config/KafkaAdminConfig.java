package com.pehlione.kafka.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaAdminConfig {

    @Bean
    public KafkaAdmin kafkaAdmin(KafkaTopicProperties topicProperties) {
        KafkaAdmin kafkaAdmin = new KafkaAdmin(buildAdminProperties(topicProperties));
        kafkaAdmin.setFatalIfBrokerNotAvailable(false);
        return kafkaAdmin;
    }

    @Bean(destroyMethod = "close")
    public AdminClient adminClient(KafkaAdmin kafkaAdmin) {
        return AdminClient.create(new HashMap<>(kafkaAdmin.getConfigurationProperties()));
    }

    private Map<String, Object> buildAdminProperties(KafkaTopicProperties topicProperties) {
        KafkaTopicProperties.Admin admin = topicProperties.getAdmin();
        Map<String, Object> config = new HashMap<>();

        put(config, AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, admin.getBootstrapServers());
        put(config, AdminClientConfig.BOOTSTRAP_CONTROLLERS_CONFIG, admin.getBootstrapControllers());
        put(config, AdminClientConfig.CLIENT_ID_CONFIG, admin.getClientId());
        put(config, AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, admin.getDefaultApiTimeoutMs());
        put(config, AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, admin.getRequestTimeoutMs());
        put(config, AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, admin.getConnectionsMaxIdleMs());
        put(config, CommonClientConfigs.CLIENT_DNS_LOOKUP_CONFIG, admin.getClientDnsLookup());
        put(config, CommonClientConfigs.RECEIVE_BUFFER_CONFIG, admin.getReceiveBufferBytes());
        return config;
    }

    private void put(Map<String, Object> config, String key, Object value) {
        if (value != null) {
            config.put(key, value);
        }
    }
}
