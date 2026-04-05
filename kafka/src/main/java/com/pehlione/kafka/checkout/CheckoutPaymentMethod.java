package com.pehlione.kafka.checkout;

public enum CheckoutPaymentMethod {
    PAYPAL("PayPal", "Hizli online odeme"),
    KLARNA("Klarna", "Sonra ode veya taksit secenegi"),
    CREDIT_CARD("Kredi Karti", "Visa, Mastercard ve Amex"),
    DEBIT_CARD("Debit Karti", "Banka karti ile guvenli odeme");

    private final String displayName;
    private final String description;

    CheckoutPaymentMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
