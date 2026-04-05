package com.pehlione.kafka.api;

import com.pehlione.kafka.dto.RabbitDlqMessageView;
import com.pehlione.kafka.dto.RabbitMailQueueOverview;
import com.pehlione.kafka.dto.RabbitQueueStats;
import com.pehlione.kafka.service.RabbitMqMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminMailQueueControllerTest {

    private RabbitMqMonitoringService rabbitMqMonitoringService;
    private AdminMailQueueController adminMailQueueController;

    @BeforeEach
    void setUp() {
        rabbitMqMonitoringService = mock(RabbitMqMonitoringService.class);
        adminMailQueueController = new AdminMailQueueController(rabbitMqMonitoringService);
    }

    @Test
    void indexShouldPopulateOverview() {
        Model model = new ExtendedModelMap();
        RabbitMailQueueOverview overview = new RabbitMailQueueOverview(
                true,
                "ok",
                new RabbitQueueStats("mail", 1, 0, 1, 1, 1.0, 1.0, "running"),
                new RabbitQueueStats("retry", 0, 0, 0, 0, 0.0, 0.0, "running"),
                new RabbitQueueStats("dlq", 1, 0, 1, 0, 0.0, 0.0, "running"),
                List.of(new RabbitDlqMessageView("mail.exchange", "mail.send", false, "payload"))
        );

        when(rabbitMqMonitoringService.fetchMailQueueOverview()).thenReturn(overview);

        String viewName = adminMailQueueController.index(model);

        assertThat(viewName).isEqualTo("admin/mail-queue/index");
        assertThat(model.getAttribute("overview")).isEqualTo(overview);
    }

    @Test
    void retryDeadLetterQueueShouldShowSuccessMessageWhenMessagesAreRetried() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(rabbitMqMonitoringService.retryDeadLetterMessages()).thenReturn(3);

        String viewName = adminMailQueueController.retryDeadLetterQueue(redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/admin/mail-queue");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("successMessage")
                .isEqualTo("3 DLQ mesaji yeniden ana kuyruga alindi.");
    }

    @Test
    void retryDeadLetterQueueShouldShowEmptyMessageWhenDlqHasNoMessages() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(rabbitMqMonitoringService.retryDeadLetterMessages()).thenReturn(0);

        String viewName = adminMailQueueController.retryDeadLetterQueue(redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/admin/mail-queue");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("successMessage")
                .isEqualTo("DLQ tarafinda yeniden denenecek mesaj bulunamadi.");
    }

    @Test
    void retryDeadLetterQueueShouldShowErrorMessageWhenRetryFails() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        when(rabbitMqMonitoringService.retryDeadLetterMessages())
                .thenThrow(new RuntimeException("retry failed"));

        String viewName = adminMailQueueController.retryDeadLetterQueue(redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/admin/mail-queue");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("errorMessage")
                .isEqualTo("retry failed");
    }
}
