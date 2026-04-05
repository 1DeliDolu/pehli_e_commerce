package com.pehlione.kafka.checkout;

public record CheckoutCustomerDetails(
        String firstName,
        String lastName,
        String email,
        String phone,
        String company,
        boolean registeredCustomer
) {

    public String fullName() {
        return (safe(firstName) + " " + safe(lastName)).trim();
    }

    public String displayName() {
        String fullName = fullName();
        if (!fullName.isBlank()) {
            return fullName;
        }
        return safe(email);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
