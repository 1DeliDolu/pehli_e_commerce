package com.pehlione.kafka.config;

import com.pehlione.kafka.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    private final CartService cartService;

    @ModelAttribute("loggedIn")
    public boolean loggedIn(Authentication authentication) {
        return SecurityConfig.isLoggedIn(authentication);
    }

    @ModelAttribute("username")
    public String username(Authentication authentication) {
        if (!SecurityConfig.isLoggedIn(authentication)) {
            return null;
        }
        return authentication.getName();
    }

    @ModelAttribute("userRole")
    public String userRole(Authentication authentication) {
        if (!SecurityConfig.isLoggedIn(authentication)) {
            return null;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    @ModelAttribute("roleDisplayName")
    public String roleDisplayName(Authentication authentication) {
        String role = userRole(authentication);
        if (role == null) {
            return null;
        }
        return switch (role) {
            case ROLE_SUPER_ADMIN -> "Super Admin";
            case ROLE_ADMIN -> "Admin";
            case "ROLE_USER" -> "User";
            default -> role.replace("ROLE_", "").replace('_', ' ');
        };
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        return hasAuthority(authentication, ROLE_ADMIN) || hasAuthority(authentication, ROLE_SUPER_ADMIN);
    }

    @ModelAttribute("isSuperAdmin")
    public boolean isSuperAdmin(Authentication authentication) {
        return hasAuthority(authentication, ROLE_SUPER_ADMIN);
    }

    @ModelAttribute("cartItemCount")
    public int cartItemCount(HttpSession session) {
        return cartService.getItemCount(session);
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        if (!SecurityConfig.isLoggedIn(authentication)) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}
