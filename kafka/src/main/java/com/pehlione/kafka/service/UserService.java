package com.pehlione.kafka.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pehlione.kafka.checkout.CheckoutForm;
import com.pehlione.kafka.model.User;
import com.pehlione.kafka.model.UserRole;
import com.pehlione.kafka.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User register(String username, String email, String rawPassword) {
        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim();
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("This username is already taken.");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("This email is already registered.");
        }
        User user = User.builder()
                .username(normalizedUsername)
                .email(normalizedEmail)
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.ROLE_USER.name())
                .build();
        return userRepository.save(user);
    }

    public User findRequiredByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User updateCheckoutProfile(String username, CheckoutForm checkoutForm) {
        User user = findRequiredByUsername(username);
        user.setFirstName(trimToNull(checkoutForm.getFirstName()));
        user.setLastName(trimToNull(checkoutForm.getLastName()));
        user.setPhone(trimToNull(checkoutForm.getPhone()));
        user.setCompany(trimToNull(checkoutForm.getCompany()));
        user.setStreet(trimToNull(checkoutForm.getStreet()));
        user.setHouseNumber(trimToNull(checkoutForm.getHouseNumber()));
        user.setAddressLine2(trimToNull(checkoutForm.getAddressLine2()));
        user.setPostalCode(trimToNull(checkoutForm.getPostalCode()));
        user.setCity(trimToNull(checkoutForm.getCity()));
        user.setCountryCode(trimToNull(checkoutForm.getCountryCode()));
        return userRepository.save(user);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
