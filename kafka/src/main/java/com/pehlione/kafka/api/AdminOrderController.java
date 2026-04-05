package com.pehlione.kafka.api;

import com.pehlione.kafka.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("orders", orderService.findAllOrdersForAdmin());
        model.addAttribute("orderSummary", orderService.buildAdminSummary());
        model.addAttribute("customerAnalytics", orderService.buildCustomerAnalytics());
        return "admin/orders/index";
    }
}
