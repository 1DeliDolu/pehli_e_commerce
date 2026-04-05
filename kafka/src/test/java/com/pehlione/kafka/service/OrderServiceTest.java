package com.pehlione.kafka.service;

import com.pehlione.kafka.checkout.CheckoutAddressDetails;
import com.pehlione.kafka.checkout.CheckoutCustomerDetails;
import com.pehlione.kafka.checkout.CheckoutPaymentMethod;
import com.pehlione.kafka.checkout.PaymentSimulationResult;
import com.pehlione.kafka.dto.CartItemView;
import com.pehlione.kafka.dto.CustomerOrderAnalyticsView;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.model.Order;
import com.pehlione.kafka.model.OrderStatus;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.model.User;
import com.pehlione.kafka.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderService = new OrderService(orderRepository);
    }

    @Test
    void createOrderShouldSnapshotCheckoutData() {
        User user = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.de")
                .password("secret")
                .build();
        CheckoutCustomerDetails customer = new CheckoutCustomerDetails(
                "Alice",
                "Doe",
                "alice@example.de",
                "+49 123",
                "Acme GmbH",
                true
        );
        CheckoutAddressDetails address = new CheckoutAddressDetails(
                "Musterstrasse",
                "15A",
                "2. Etage",
                "10115",
                "Berlin",
                "DE"
        );
        PaymentSimulationResult payment = new PaymentSimulationResult(
                CheckoutPaymentMethod.PAYPAL,
                "APPROVED",
                "PP-TX-11111111",
                "PP-SIM-111111111111",
                "PayPal",
                "Simulated",
                BigDecimal.valueOf(49.90),
                LocalDateTime.now(),
                true
        );

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = orderService.createOrder(
                "session-1",
                user,
                customer,
                address,
                CheckoutPaymentMethod.PAYPAL,
                payment,
                List.of(sampleCartItem()),
                BigDecimal.valueOf(49.90)
        );

        assertThat(order.getOrderNumber()).startsWith("ORD-");
        assertThat(order.getUser()).isEqualTo(user);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED.name());
        assertThat(order.getCustomerEmail()).isEqualTo("alice@example.de");
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().getFirst().getProductName()).isEqualTo("Kafka Book");
    }

    @Test
    void cancelOrderForUserShouldMarkOrderCanceled() {
        Order order = Order.builder()
                .id(10L)
                .orderNumber("ORD-20260405-CANCEL")
                .status(OrderStatus.PLACED.name())
                .sessionId("session-1")
                .customerFirstName("Alice")
                .customerLastName("Doe")
                .customerEmail("alice@example.de")
                .customerPhone("+49 123")
                .street("Musterstrasse")
                .houseNumber("10")
                .postalCode("10115")
                .city("Berlin")
                .countryCode("DE")
                .paymentMethod("PAYPAL")
                .paymentStatus("APPROVED")
                .paymentTransactionId("PP-TX")
                .paymentProviderReference("PP-REF")
                .subtotal(BigDecimal.TEN)
                .build();

        when(orderRepository.findByIdAndUserUsername(10L, "alice")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order canceled = orderService.cancelOrderForUser(10L, "alice", "Vazgecti");

        assertThat(canceled.getStatus()).isEqualTo(OrderStatus.CANCELED.name());
        assertThat(canceled.getCancelReason()).isEqualTo("Vazgecti");
        assertThat(canceled.getCanceledAt()).isNotNull();
    }

    @Test
    void buildCustomerAnalyticsShouldCalculateFrequencyAndCanceledCount() {
        Order first = analyticsOrder("ORD-1", "alice@example.de", "Alice", "Doe", OrderStatus.PLACED, LocalDateTime.of(2026, 4, 1, 10, 0), null);
        Order second = analyticsOrder("ORD-2", "alice@example.de", "Alice", "Doe", OrderStatus.PLACED, LocalDateTime.of(2026, 4, 11, 10, 0), null);
        Order third = analyticsOrder("ORD-3", "alice@example.de", "Alice", "Doe", OrderStatus.CANCELED, LocalDateTime.of(2026, 4, 21, 10, 0), LocalDateTime.of(2026, 4, 21, 11, 0));

        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(third, second, first));

        List<CustomerOrderAnalyticsView> metrics = orderService.buildCustomerAnalytics();

        assertThat(metrics).hasSize(1);
        CustomerOrderAnalyticsView metric = metrics.getFirst();
        assertThat(metric.totalOrders()).isEqualTo(3);
        assertThat(metric.canceledOrders()).isEqualTo(1);
        assertThat(metric.lastCanceledOrderNumber()).isEqualTo("ORD-3");
        assertThat(metric.averageOrderIntervalDays()).isEqualTo(10.0d);
    }

    private CartItemView sampleCartItem() {
        Category category = Category.builder().id(1L).name("Books").build();
        Product product = Product.builder()
                .id(2L)
                .name("Kafka Book")
                .price(BigDecimal.valueOf(49.90))
                .category(category)
                .build();
        return new CartItemView(product, 1, BigDecimal.valueOf(49.90));
    }

    private Order analyticsOrder(String orderNumber,
                                 String email,
                                 String firstName,
                                 String lastName,
                                 OrderStatus status,
                                 LocalDateTime createdAt,
                                 LocalDateTime canceledAt) {
        return Order.builder()
                .orderNumber(orderNumber)
                .sessionId("session")
                .customerFirstName(firstName)
                .customerLastName(lastName)
                .customerEmail(email)
                .customerPhone("+49 123")
                .street("Musterstrasse")
                .houseNumber("1")
                .postalCode("10115")
                .city("Berlin")
                .countryCode("DE")
                .paymentMethod("PAYPAL")
                .paymentStatus("APPROVED")
                .paymentTransactionId("PP-TX")
                .paymentProviderReference("PP-REF")
                .subtotal(BigDecimal.TEN)
                .status(status.name())
                .cancelReason(canceledAt == null ? null : "Kullanici talebi")
                .canceledAt(canceledAt)
                .createdAt(createdAt)
                .build();
    }
}
