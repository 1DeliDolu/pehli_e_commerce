package com.pehlione.kafka.service;

import com.pehlione.kafka.checkout.CheckoutCustomerDetails;
import com.pehlione.kafka.checkout.CheckoutPaymentMethod;
import com.pehlione.kafka.checkout.PaymentSimulationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentSimulationServiceTest {

    private PaymentSimulationService paymentSimulationService;

    @BeforeEach
    void setUp() {
        paymentSimulationService = new PaymentSimulationService();
    }

    @Test
    void simulateShouldReturnApprovedPaypalResult() {
        CheckoutCustomerDetails customer = new CheckoutCustomerDetails(
                "Alice",
                "Smith",
                "alice@example.com",
                "123",
                null,
                true
        );

        PaymentSimulationResult result = paymentSimulationService.simulate(
                CheckoutPaymentMethod.PAYPAL,
                BigDecimal.valueOf(99.90),
                customer
        );

        assertThat(result.simulated()).isTrue();
        assertThat(result.status()).isEqualTo("APPROVED");
        assertThat(result.transactionId()).startsWith("PP-TX-");
        assertThat(result.providerReference()).startsWith("PP-SIM-");
        assertThat(result.providerLabel()).isEqualTo("PayPal");
        assertThat(result.message()).isEqualTo("PayPal simulation for Alice Smith was successful.");
        assertThat(result.amount()).isEqualByComparingTo("99.90");
    }

    @Test
    void simulateShouldReturnCreditCardSpecificMessage() {
        CheckoutCustomerDetails customer = new CheckoutCustomerDetails(
                "Alice",
                "Smith",
                "alice@example.com",
                "123",
                null,
                true
        );

        PaymentSimulationResult result = paymentSimulationService.simulate(
                CheckoutPaymentMethod.CREDIT_CARD,
                BigDecimal.valueOf(149.90),
                customer
        );

        assertThat(result.transactionId()).startsWith("CC-TX-");
        assertThat(result.providerReference()).startsWith("CC-SIM-");
        assertThat(result.providerLabel()).isEqualTo("Kredi Karti");
        assertThat(result.message()).isEqualTo("Credit card authorization was successfully simulated in the test environment.");
    }
}
