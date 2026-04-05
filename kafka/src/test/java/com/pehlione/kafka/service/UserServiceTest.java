package com.pehlione.kafka.service;

import com.pehlione.kafka.checkout.CheckoutForm;
import com.pehlione.kafka.model.User;
import com.pehlione.kafka.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void registerShouldTrimValuesAndEncodePassword() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(" alice ", " alice@example.com ", "secret123");

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("alice", "alice@example.com", "secret123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This username is already taken.");
    }

    @Test
    void findRequiredByUsernameShouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findRequiredByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: ghost");
    }

    @Test
    void updateCheckoutProfileShouldTrimAndNullBlankFields() {
        User existing = User.builder()
                .id(1L)
                .username("alice")
                .email("alice@example.com")
                .password("encoded")
                .build();
        CheckoutForm form = new CheckoutForm();
        form.setFirstName(" Alice ");
        form.setLastName(" Smith ");
        form.setPhone(" 123 ");
        form.setCompany("   ");
        form.setStreet(" Main Street ");
        form.setHouseNumber(" 10A ");
        form.setAddressLine2("  ");
        form.setPostalCode(" 10115 ");
        form.setCity(" Berlin ");
        form.setCountryCode(" DE ");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userService.updateCheckoutProfile("alice", form);

        assertThat(result.getFirstName()).isEqualTo("Alice");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getPhone()).isEqualTo("123");
        assertThat(result.getCompany()).isNull();
        assertThat(result.getAddressLine2()).isNull();
        assertThat(result.getPostalCode()).isEqualTo("10115");
        assertThat(result.getCountryCode()).isEqualTo("DE");
    }
}
