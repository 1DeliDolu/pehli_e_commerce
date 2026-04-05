package com.pehlione.kafka.api;

import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.model.Order;
import com.pehlione.kafka.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CommerceEventPublisher commerceEventPublisher;

    @GetMapping
    public String index(Authentication authentication, Model model) {
        model.addAttribute("orders", orderService.findOrdersForUser(authentication.getName()));
        model.addAttribute("orderSummary", orderService.buildUserSummary(authentication.getName()));
        return "orders/index";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.cancelOrderForUser(id, authentication.getName(), "Kullanici panelinden iptal edildi");
            commerceEventPublisher.publishOrderCancelled(order);
            redirectAttributes.addFlashAttribute("successMessage", "Siparis iptal edildi: " + order.getOrderNumber());
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/orders";
    }
}
