package com.pehlione.kafka.api;

import com.pehlione.kafka.dto.KafkaAdminOverview;
import com.pehlione.kafka.service.KafkaAdminMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminKafkaTopicControllerTest {

    private KafkaAdminMonitoringService kafkaAdminMonitoringService;
    private AdminKafkaTopicController adminKafkaTopicController;

    @BeforeEach
    void setUp() {
        kafkaAdminMonitoringService = mock(KafkaAdminMonitoringService.class);
        adminKafkaTopicController = new AdminKafkaTopicController(kafkaAdminMonitoringService);
    }

    @Test
    void indexShouldPopulateOverview() {
        Model model = new ExtendedModelMap();
        KafkaAdminOverview overview = new KafkaAdminOverview(
                true,
                "ok",
                "localhost:9092",
                "topic-admin",
                5,
                List.of()
        );

        when(kafkaAdminMonitoringService.fetchOverview()).thenReturn(overview);

        String viewName = adminKafkaTopicController.index(model);

        assertThat(viewName).isEqualTo("admin/topics/index");
        assertThat(model.getAttribute("overview")).isEqualTo(overview);
    }
}
