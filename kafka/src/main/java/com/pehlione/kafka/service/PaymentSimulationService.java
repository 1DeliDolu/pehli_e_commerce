package com.pehlione.kafka.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.pehlione.kafka.checkout.CheckoutCustomerDetails;
import com.pehlione.kafka.checkout.CheckoutPaymentMethod;
import com.pehlione.kafka.checkout.PaymentSimulationResult;

@Service
public class PaymentSimulationService {

    public PaymentSimulationResult simulate(CheckoutPaymentMethod paymentMethod,
                                            BigDecimal amount,
                                            CheckoutCustomerDetails customerDetails) {
        LocalDateTime approvedAt = LocalDateTime.now();
        String transactionId = buildTransactionId(paymentMethod);
        String providerReference = buildProviderReference(paymentMethod);

        return new PaymentSimulationResult(
                paymentMethod,
                "APPROVED",
                transactionId,
                providerReference,
                paymentMethod.getDisplayName(),
                buildMessage(paymentMethod, customerDetails),
                amount,
                approvedAt,
                true
        );
    }

    private String buildTransactionId(CheckoutPaymentMethod paymentMethod) {
        return prefix(paymentMethod) + "-TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String buildProviderReference(CheckoutPaymentMethod paymentMethod) {
        return prefix(paymentMethod) + "-SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase(Locale.ROOT);
    }

    private String buildMessage(CheckoutPaymentMethod paymentMethod, CheckoutCustomerDetails customerDetails) {
        String customerName = customerDetails.displayName();
        return switch (paymentMethod) {
            case PAYPAL -> "PayPal simulation for " + customerName + " was successful.";
            case KLARNA -> "Klarna test payment for " + customerName + " was approved.";
            case CREDIT_CARD -> "Credit card authorization was successfully simulated in the test environment.";
            case DEBIT_CARD -> "Debit card payment was successfully simulated in the test environment.";
        };
    }

    private String prefix(CheckoutPaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case PAYPAL -> "PP";
            case KLARNA -> "KL";
            case CREDIT_CARD -> "CC";
            case DEBIT_CARD -> "DC";
        };
    }
}
