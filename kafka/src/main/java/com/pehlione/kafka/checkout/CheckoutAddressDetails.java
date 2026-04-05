package com.pehlione.kafka.checkout;

public record CheckoutAddressDetails(
        String street,
        String houseNumber,
        String addressLine2,
        String postalCode,
        String city,
        String countryCode
) {

    public String formattedMultiline() {
        StringBuilder builder = new StringBuilder();
        builder.append(street).append(' ').append(houseNumber).append('\n');
        if (addressLine2 != null && !addressLine2.isBlank()) {
            builder.append(addressLine2).append('\n');
        }
        builder.append(postalCode).append(' ').append(city).append('\n');
        builder.append(countryDisplayName());
        return builder.toString();
    }

    public String countryDisplayName() {
        if ("DE".equalsIgnoreCase(countryCode)) {
            return "Deutschland";
        }
        return countryCode;
    }
}
