package com.pehlione.kafka.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterForm {
    @NotBlank(message = "Kullanici adi zorunludur.")
    @Size(max = 50, message = "Kullanici adi en fazla 50 karakter olabilir.")
    private String username;

    @NotBlank(message = "E-posta zorunludur.")
    @Email(message = "Gecerli bir e-posta girin.")
    @Size(max = 100, message = "E-posta en fazla 100 karakter olabilir.")
    private String email;

    @NotBlank(message = "Sifre zorunludur.")
    @Size(min = 6, max = 100, message = "Sifre 6-100 karakter araliginda olmalidir.")
    private String password;
}
