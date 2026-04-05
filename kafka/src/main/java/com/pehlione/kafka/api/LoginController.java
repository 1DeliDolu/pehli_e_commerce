package com.pehlione.kafka.api;

import com.pehlione.kafka.config.SecurityConfig;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(Model model, Authentication authentication) {
        model.addAttribute("loggedIn", SecurityConfig.isLoggedIn(authentication));
        return "login";
    }
}
