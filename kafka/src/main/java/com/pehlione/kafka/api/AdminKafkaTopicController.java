package com.pehlione.kafka.api;

import com.pehlione.kafka.dto.KafkaAdminOverview;
import com.pehlione.kafka.service.KafkaAdminMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/topics")
@RequiredArgsConstructor
public class AdminKafkaTopicController {

    private final KafkaAdminMonitoringService kafkaAdminMonitoringService;

    @GetMapping
    public String index(Model model) {
        KafkaAdminOverview overview = kafkaAdminMonitoringService.fetchOverview();
        model.addAttribute("overview", overview);
        return "admin/topics/index";
    }
}
