package com.pehlione.kafka.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pehlione.kafka.checkout.CheckoutAddressDetails;
import com.pehlione.kafka.checkout.CheckoutCustomerDetails;
import com.pehlione.kafka.checkout.CheckoutPaymentMethod;
import com.pehlione.kafka.checkout.PaymentSimulationResult;
import com.pehlione.kafka.dto.CartItemView;
import com.pehlione.kafka.dto.CustomerOrderAnalyticsView;
import com.pehlione.kafka.dto.OrderDashboardSummary;
import com.pehlione.kafka.model.Order;
import com.pehlione.kafka.model.OrderItem;
import com.pehlione.kafka.model.OrderStatus;
import com.pehlione.kafka.model.User;
import com.pehlione.kafka.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(String sessionId,
                             User user,
                             CheckoutCustomerDetails customer,
                             CheckoutAddressDetails shippingAddress,
                             CheckoutPaymentMethod paymentMethod,
                             PaymentSimulationResult paymentSimulationResult,
                             List<CartItemView> cartItems,
                             BigDecimal subtotal) {
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .sessionId(sessionId)
                .customerFirstName(customer.firstName())
                .customerLastName(customer.lastName())
                .customerEmail(customer.email())
                .customerPhone(customer.phone())
                .customerCompany(normalize(customer.company()))
                .street(shippingAddress.street())
                .houseNumber(shippingAddress.houseNumber())
                .addressLine2(normalize(shippingAddress.addressLine2()))
                .postalCode(shippingAddress.postalCode())
                .city(shippingAddress.city())
                .countryCode(shippingAddress.countryCode())
                .paymentMethod(paymentMethod.name())
                .paymentStatus(paymentSimulationResult.status())
                .paymentTransactionId(paymentSimulationResult.transactionId())
                .paymentProviderReference(paymentSimulationResult.providerReference())
                .paymentMessage(normalize(paymentSimulationResult.message()))
                .subtotal(subtotal)
                .status(OrderStatus.PLACED.name())
                .build();

        for (CartItemView item : cartItems) {
            order.addItem(OrderItem.builder()
                    .productId(item.product().getId())
                    .productName(item.product().getName())
                    .quantity(item.quantity())
                    .unitPrice(item.product().getPrice())
                    .lineTotal(item.lineTotal())
                    .build());
        }

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> findOrdersForUser(String username) {
        return orderRepository.findAllByUserUsernameOrderByCreatedAtDesc(username);
    }

    @Transactional(readOnly = true)
    public OrderDashboardSummary buildUserSummary(String username) {
        return buildSummary(findOrdersForUser(username));
    }

    @Transactional(readOnly = true)
    public List<Order> findAllOrdersForAdmin() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public OrderDashboardSummary buildAdminSummary() {
        return buildSummary(findAllOrdersForAdmin());
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderAnalyticsView> buildCustomerAnalytics() {
        Map<String, List<Order>> ordersByCustomer = new LinkedHashMap<>();
        for (Order order : findAllOrdersForAdmin()) {
            String key = order.getCustomerEmail().toLowerCase(Locale.ROOT);
            ordersByCustomer.computeIfAbsent(key, ignored -> new ArrayList<>()).add(order);
        }

        return ordersByCustomer.values().stream()
                .map(this::toCustomerAnalytics)
                .sorted(Comparator.comparing(CustomerOrderAnalyticsView::totalOrders).reversed()
                        .thenComparing(CustomerOrderAnalyticsView::lastOrderAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public Order cancelOrderForUser(Long orderId, String username, String cancelReason) {
        Order order = orderRepository.findByIdAndUserUsername(orderId, username)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (!order.canBeCanceled()) {
            throw new IllegalStateException("This order can no longer be canceled.");
        }

        order.setStatus(OrderStatus.CANCELED.name());
        order.setCancelReason(normalize(cancelReason) == null ? "User request" : normalize(cancelReason));
        order.setCanceledAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private OrderDashboardSummary buildSummary(List<Order> orders) {
        long canceledOrders = orders.stream()
                .filter(order -> OrderStatus.CANCELED.name().equals(order.getStatus()))
                .count();
        long placedOrders = orders.size() - canceledOrders;
        long distinctCustomers = orders.stream()
                .map(order -> order.getCustomerEmail().toLowerCase(Locale.ROOT))
                .distinct()
                .count();
        long repeatCustomers = orders.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        order -> order.getCustomerEmail().toLowerCase(Locale.ROOT),
                        java.util.stream.Collectors.counting()))
                .values().stream()
                .filter(count -> count > 1)
                .count();
        return new OrderDashboardSummary(orders.size(), placedOrders, canceledOrders, distinctCustomers, repeatCustomers);
    }

    private CustomerOrderAnalyticsView toCustomerAnalytics(List<Order> customerOrders) {
        List<Order> sortedAscending = customerOrders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt))
                .toList();

        Order latestOrder = sortedAscending.get(sortedAscending.size() - 1);
        String lastCanceledOrderNumber = customerOrders.stream()
                .filter(order -> OrderStatus.CANCELED.name().equals(order.getStatus()))
                .max(Comparator.comparing(Order::getCanceledAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(Order::getOrderNumber)
                .orElse(null);

        return new CustomerOrderAnalyticsView(
                latestOrder.customerDisplayName(),
                latestOrder.getCustomerEmail(),
                customerOrders.size(),
                customerOrders.stream().filter(order -> OrderStatus.CANCELED.name().equals(order.getStatus())).count(),
                lastCanceledOrderNumber,
                latestOrder.getCreatedAt(),
                calculateAverageIntervalDays(sortedAscending)
        );
    }

    private Double calculateAverageIntervalDays(List<Order> orders) {
        if (orders.size() < 2) {
            return null;
        }

        double totalDays = 0;
        for (int i = 1; i < orders.size(); i++) {
            Duration duration = Duration.between(orders.get(i - 1).getCreatedAt(), orders.get(i).getCreatedAt());
            totalDays += duration.toHours() / 24.0d;
        }
        return totalDays / (orders.size() - 1);
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().format(ORDER_NUMBER_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
