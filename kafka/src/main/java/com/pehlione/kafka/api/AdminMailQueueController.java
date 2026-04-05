package com.pehlione.kafka.api;

import com.pehlione.kafka.dto.RabbitMailQueueOverview;
import com.pehlione.kafka.service.RabbitMqMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/mail-queue")
@RequiredArgsConstructor
public class AdminMailQueueController {

    private final RabbitMqMonitoringService rabbitMqMonitoringService;

    @GetMapping
    public String index(Model model) {
        RabbitMailQueueOverview overview = rabbitMqMonitoringService.fetchMailQueueOverview();
        model.addAttribute("overview", overview);
        return "admin/mail-queue/index";
    }

    @PostMapping("/retry-dlq")
    public String retryDeadLetterQueue(RedirectAttributes redirectAttributes) {
        try {
            int retriedCount = rabbitMqMonitoringService.retryDeadLetterMessages();
            if (retriedCount == 0) {
                redirectAttributes.addFlashAttribute("successMessage", "DLQ tarafinda yeniden denenecek mesaj bulunamadi.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", retriedCount + " DLQ mesaji yeniden ana kuyruga alindi.");
            }
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/mail-queue";
    }
}
