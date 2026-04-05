package com.pehlione.kafka.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pehlione.kafka.checkout.CheckoutAddressDetails;
import com.pehlione.kafka.checkout.CheckoutCustomerDetails;
import com.pehlione.kafka.checkout.CheckoutPaymentMethod;
import com.pehlione.kafka.checkout.PaymentSimulationResult;
import com.pehlione.kafka.config.KafkaTopicProperties;
import com.pehlione.kafka.dto.AdminCategoryEvent;
import com.pehlione.kafka.dto.AdminProductEvent;
import com.pehlione.kafka.dto.CartEvent;
import com.pehlione.kafka.dto.CartItemView;
import com.pehlione.kafka.dto.CategoryViewedEvent;
import com.pehlione.kafka.dto.CheckoutEvent;
import com.pehlione.kafka.dto.CheckoutItemEvent;
import com.pehlione.kafka.dto.MailJobMessage;
import com.pehlione.kafka.dto.MailRequestedEvent;
import com.pehlione.kafka.dto.OrderCancelledEvent;
import com.pehlione.kafka.dto.OrderMailEvent;
import com.pehlione.kafka.dto.ProductViewedEvent;
import com.pehlione.kafka.dto.UserLoginEvent;
import com.pehlione.kafka.dto.UserRegisteredEvent;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.model.Order;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.model.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommerceEventPublisher {

    private final KafkaMessageProducer kafkaMessageProducer;
    private final KafkaTopicProperties kafkaTopicProperties;
    private final ObjectMapper objectMapper;

    public void publishCartEvent(String eventType,
                                 HttpSession session,
                                 Long productId,
                                 String productName,
                                 int quantity) {
        CartEvent event = new CartEvent(
                eventType,
                session.getId(),
                productId,
                productName,
                quantity,
                LocalDateTime.now()
        );
        publishSafely(
                kafkaTopicProperties.getTopics().getUserClick().getName(),
                session.getId(),
                eventType,
                event
        );
    }

    public void publishUserRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "user.registered",
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                LocalDateTime.now()
        );
        publishSafely(
                kafkaTopicProperties.getTopics().getAuditEvent().getName(),
                user.getUsername(),
                "user.registered",
                event
        );
    }

    public void publishUserLoggedIn(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("ROLE_USER");

        UserLoginEvent event = new UserLoginEvent(
                "user.logged_in",
                authentication.getName(),
                role,
                "SUCCESS",
                LocalDateTime.now()
        );
        publishSafely(
                kafkaTopicProperties.getTopics().getUserLogin().getName(),
                authentication.getName(),
                "user.logged_in",
                event
        );
    }

    public void publishMailRequested(MailJobMessage job) {
        MailRequestedEvent event = new MailRequestedEvent(
                "mail.requested",
                job.jobId(),
                job.recipientName(),
                job.recipientEmail(),
                job.itemCount(),
                job.subtotal(),
                LocalDateTime.now()
        );
        publishSafely(
                kafkaTopicProperties.getTopics().getMailRequested().getName(),
                job.jobId(),
                "mail.requested",
                event
        );
    }

    public void publishOrderEmailSent(User user, List<CartItemView> cartItems, BigDecimal subtotal) {
        publishOrderEmailSent(
                user.getUsername(),
                user.getEmail(),
                cartItems.stream().mapToInt(CartItemView::quantity).sum(),
                subtotal
        );
    }

    public void publishOrderEmailSent(String username,
                                      String recipientEmail,
                                      int itemCount,
                                      BigDecimal subtotal) {
        OrderMailEvent event = new OrderMailEvent(
                "order.email.sent",
                username,
                recipientEmail,
                itemCount,
                subtotal,
                LocalDateTime.now()
        );
        publishSafely(
                kafkaTopicProperties.getTopics().getAuditEvent().getName(),
                username,
                "order.email.sent",
                event
        );
    }

    public void publishAdminProductEvent(String eventType, String actorUsername, Product product) {
        AdminProductEvent event = new AdminProductEvent(
                eventType,
                actorUsername,
                product.getId(),
                product.getName(),
                product.getCategory().getName(),
                product.getPrice(),
                LocalDateTime.now()
        );
        publishSafely(
                kafkaTopicProperties.getTopics().getAuditEvent().getName(),
                actorUsername,
                eventType,
                event
        );
    }

    public void publishAdminCategoryEvent(String eventType, String actorUsername, Category category) {
        AdminCategoryEvent event = new AdminCategoryEvent(
                eventType,
                actorUsername,
                category.getId(),
                category.getName(),
                LocalDateTime.now()
        );
        publishSafely(
                kafkaTopicProperties.getTopics().getAuditEvent().getName(),
                actorUsername,
                eventType,
                event
        );
    }

    public void publishProductViewed(HttpSession session, Product product) {
        ProductViewedEvent event = new ProductViewedEvent(
                "product.viewed",
                session.getId(),
                product.getId(),
                product.getName(),
                product.getCategory().getName(),
                product.getPrice(),
                LocalDateTime.now()
        );
        publishSafely(
                kafkaTopicProperties.getTopics().getUserClick().getName(),
                session.getId(),
                "product.viewed",
                event
        );
    }

    public void publishCategoryViewed(HttpSession session, Category category) {
        CategoryViewedEvent event = new CategoryViewedEvent(
                "category.viewed",
                session.getId(),
                category.getId(),
                category.getName(),
                LocalDateTime.now()
        );
        publishSafely(
                kafkaTopicProperties.getTopics().getUserClick().getName(),
                session.getId(),
                "category.viewed",
                event
        );
    }

    public void publishCheckoutCompleted(HttpSession session,
                                         Order order,
                                         List<CartItemView> cartItems,
                                         BigDecimal subtotal,
                                         CheckoutCustomerDetails customer,
                                         CheckoutAddressDetails shippingAddress,
                                         CheckoutPaymentMethod paymentMethod,
                                         PaymentSimulationResult paymentSimulationResult) {
        List<CheckoutItemEvent> items = cartItems.stream()
                .map(item -> new CheckoutItemEvent(
                        item.product().getId(),
                        item.product().getName(),
                        item.quantity(),
                        item.product().getPrice(),
                        item.lineTotal()
                ))
                .toList();

        CheckoutEvent event = new CheckoutEvent(
                "order.created",
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                session.getId(),
                cartItems.stream().mapToInt(CartItemView::quantity).sum(),
                subtotal,
                customer,
                shippingAddress,
                paymentMethod,
                paymentSimulationResult,
                items,
                LocalDateTime.now()
        );

        publishSafely(
                kafkaTopicProperties.getTopics().getOrderCreated().getName(),
                session.getId(),
                "order.created",
                event
        );
    }

    public void publishOrderCancelled(Order order) {
        OrderCancelledEvent event = new OrderCancelledEvent(
                "order.cancelled",
                order.getId(),
                order.getOrderNumber(),
                order.getSessionId(),
                order.getCustomerEmail(),
                order.customerDisplayName(),
                order.getSubtotal(),
                order.getCancelReason(),
                order.getCanceledAt()
        );
        publishSafely(
                kafkaTopicProperties.getTopics().getOrderCancelled().getName(),
                order.getOrderNumber(),
                "order.cancelled",
                event
        );
    }

    private void publishSafely(String topic, String key, String eventType, Object payload) {
        try {
            kafkaMessageProducer.send(topic, key, asJson(payload));
    } catch (RuntimeException exception) {
                // Optionally implement fallback or retry logic here
            log.warn("The Kafka event could not be sent. topic={}, eventType={}, key={}", topic, eventType, key, exception);
        // Optionally implement fallback or retry logic here
        }
    }

    private String asJson(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Kafka event could not be serialized.", exception);
        }
    }
}
