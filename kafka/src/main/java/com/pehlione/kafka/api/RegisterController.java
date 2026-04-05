package com.pehlione.kafka.api;

import com.pehlione.kafka.auth.RegisterForm;
import com.pehlione.kafka.config.SecurityConfig;
import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.model.User;
import com.pehlione.kafka.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;
    private final CommerceEventPublisher commerceEventPublisher;

    @GetMapping("/register")
    public String registerPage(Model model, Authentication authentication) {
        model.addAttribute("loggedIn", SecurityConfig.isLoggedIn(authentication));
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute RegisterForm registerForm,
                             BindingResult bindingResult,
                             Model model,
                             Authentication authentication) {
        model.addAttribute("loggedIn", SecurityConfig.isLoggedIn(authentication));
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            User user = userService.register(registerForm.getUsername(), registerForm.getEmail(), registerForm.getPassword());
            commerceEventPublisher.publishUserRegistered(user);
            model.addAttribute("successMessage", "Kayıt başarılı: " + registerForm.getUsername());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }
}
