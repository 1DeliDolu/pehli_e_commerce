package com.pehlione.kafka.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.pehlione.kafka.checkout.CheckoutAddressDetails;
import com.pehlione.kafka.checkout.CheckoutCustomerDetails;
import com.pehlione.kafka.checkout.CheckoutPaymentMethod;
import com.pehlione.kafka.checkout.PaymentSimulationResult;
import com.pehlione.kafka.dto.CartItemView;
import com.pehlione.kafka.dto.MailJobMessage;
import com.pehlione.kafka.messaging.CommerceEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderMailService {

    private static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JavaMailSender mailSender;
    private final CommerceEventPublisher commerceEventPublisher;

    public MailJobMessage createCheckoutMailJob(CheckoutCustomerDetails customer,
                                                String orderNumber,
                                                CheckoutAddressDetails shippingAddress,
                                                CheckoutPaymentMethod paymentMethod,
                                                PaymentSimulationResult paymentSimulationResult,
                                                List<CartItemView> cartItems,
                                                BigDecimal subtotal) {
        return new MailJobMessage(
                UUID.randomUUID().toString(),
                customer.displayName(),
                customer.email(),
                "Order number - " + orderNumber,
                buildMessageBody(customer, orderNumber, shippingAddress, paymentMethod, paymentSimulationResult, cartItems, subtotal),
                cartItems.stream().mapToInt(CartItemView::quantity).sum(),
                subtotal,
                "checkout.completed",
                0,
                LocalDateTime.now()
        );
    }

    public void send(MailJobMessage job) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@kafka-app.local");
        message.setTo(job.recipientEmail());
        message.setSubject(job.subject());
        message.setText(job.body());
        mailSender.send(message);
        commerceEventPublisher.publishOrderEmailSent(
                job.recipientName(),
                job.recipientEmail(),
                job.itemCount(),
                job.subtotal()
        );
    }

    private String buildMessageBody(CheckoutCustomerDetails customer,
                                    String orderNumber,
                                    CheckoutAddressDetails shippingAddress,
                                    CheckoutPaymentMethod paymentMethod,
                                    PaymentSimulationResult paymentSimulationResult,
                                    List<CartItemView> cartItems,
                                    BigDecimal subtotal) {
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(customer.displayName()).append(",\n\n");
        body.append("Your order has been received. The summary is as follows:\n\n");
        body.append("Order number: ").append(orderNumber).append('\n');
        body.append("Customer type: ")
                .append(customer.registeredCustomer() ? "Account holder" : "Guest customer")
                .append('\n');
        body.append("Email: ").append(customer.email()).append('\n');
        body.append("Phone: ").append(customer.phone()).append('\n');
        if (customer.company() != null && !customer.company().isBlank()) {
            body.append("Company: ").append(customer.company()).append('\n');
        }
        body.append('\n');
        body.append("Shipping address:\n")
                .append(shippingAddress.formattedMultiline())
                .append("\n\n");
        body.append("Payment method: ")
                .append(paymentMethod.getDisplayName())
                .append('\n');
        body.append("Payment status: ")
                .append(paymentSimulationResult.status())
                .append(" (simulated)\n");
        body.append("Transaction ID: ")
                .append(paymentSimulationResult.transactionId())
                .append('\n');
        body.append("Provider reference: ")
                .append(paymentSimulationResult.providerReference())
                .append('\n');
        body.append("Payment note: ")
                .append(paymentSimulationResult.message())
                .append("\n\n");

        for (CartItemView item : cartItems) {
            body.append("- ")
                    .append(item.product().getName())
                    .append(" | Quantity: ")
                    .append(item.quantity())
                    .append(" | Amount: ")
                    .append(formatCurrency(item.lineTotal()))
                    .append('\n');
        }

        body.append("\nTotal: ").append(formatCurrency(subtotal)).append('\n');
        body.append("Order date: ").append(LocalDateTime.now().format(ORDER_TIME_FORMAT)).append("\n\n");
        body.append("This email was sent for testing purposes via MailHog.\n");
        body.append("Kafka App");
        return body.toString();
    }

    private String formatCurrency(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " EUR";
    }
}
