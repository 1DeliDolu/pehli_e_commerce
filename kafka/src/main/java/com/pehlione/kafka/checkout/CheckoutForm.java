package com.pehlione.kafka.checkout;

import com.pehlione.kafka.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutForm {

    @NotBlank(message = "Ad zorunludur.")
    @Size(max = 80, message = "Ad en fazla 80 karakter olabilir.")
    private String firstName;

    @NotBlank(message = "Soyad zorunludur.")
    @Size(max = 80, message = "Soyad en fazla 80 karakter olabilir.")
    private String lastName;

    @NotBlank(message = "E-posta zorunludur.")
    @Email(message = "Gecerli bir e-posta girin.")
    @Size(max = 100, message = "E-posta en fazla 100 karakter olabilir.")
    private String email;

    @NotBlank(message = "Telefon zorunludur.")
    @Size(max = 30, message = "Telefon en fazla 30 karakter olabilir.")
    private String phone;

    @Size(max = 120, message = "Sirket alani en fazla 120 karakter olabilir.")
    private String company;

    @NotBlank(message = "Sokak zorunludur.")
    @Size(max = 120, message = "Sokak en fazla 120 karakter olabilir.")
    private String street;

    @NotBlank(message = "Bina numarasi zorunludur.")
    @Size(max = 20, message = "Bina numarasi en fazla 20 karakter olabilir.")
    private String houseNumber;

    @Size(max = 120, message = "Ek adres bilgisi en fazla 120 karakter olabilir.")
    private String addressLine2;

    @NotBlank(message = "Posta kodu zorunludur.")
    @Pattern(regexp = "\\d{5}", message = "Almanya posta kodu 5 haneli olmalidir.")
    private String postalCode;

    @NotBlank(message = "Sehir zorunludur.")
    @Size(max = 80, message = "Sehir en fazla 80 karakter olabilir.")
    private String city;

    @NotBlank(message = "Ulke zorunludur.")
    @Pattern(regexp = "DE", message = "Checkout yalnizca Almanya adresi kabul ediyor.")
    private String countryCode = "DE";

    @NotNull(message = "Odeme yontemi secin.")
    private CheckoutPaymentMethod paymentMethod = CheckoutPaymentMethod.PAYPAL;

    public static CheckoutForm fromUser(User user) {
        CheckoutForm form = new CheckoutForm();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setCompany(user.getCompany());
        form.setStreet(user.getStreet());
        form.setHouseNumber(user.getHouseNumber());
        form.setAddressLine2(user.getAddressLine2());
        form.setPostalCode(user.getPostalCode());
        form.setCity(user.getCity());
        form.setCountryCode(user.getCountryCode() == null || user.getCountryCode().isBlank() ? "DE" : user.getCountryCode());
        return form;
    }

    public CheckoutCustomerDetails toCustomerDetails(boolean registeredCustomer) {
        return new CheckoutCustomerDetails(
                firstName,
                lastName,
                email,
                phone,
                company,
                registeredCustomer
        );
    }

    public CheckoutAddressDetails toAddressDetails() {
        return new CheckoutAddressDetails(
                street,
                houseNumber,
                addressLine2,
                postalCode,
                city,
                countryCode
        );
    }
}
