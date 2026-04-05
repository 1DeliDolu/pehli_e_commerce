package com.pehlione.kafka.dto;

import com.pehlione.kafka.checkout.CheckoutAddressDetails;
import com.pehlione.kafka.checkout.CheckoutCustomerDetails;
import com.pehlione.kafka.checkout.CheckoutPaymentMethod;
import com.pehlione.kafka.checkout.PaymentSimulationResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CheckoutEvent(
        String eventType,
        Long orderId,
        String orderNumber,
        String orderStatus,
        String sessionId,
        int itemCount,
        BigDecimal subtotal,
        CheckoutCustomerDetails customer,
        CheckoutAddressDetails shippingAddress,
        CheckoutPaymentMethod paymentMethod,
        PaymentSimulationResult payment,
        List<CheckoutItemEvent> items,
        LocalDateTime occurredAt
) {
}
