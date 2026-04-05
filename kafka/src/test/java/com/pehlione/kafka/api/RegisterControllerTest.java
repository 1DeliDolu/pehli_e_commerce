package com.pehlione.kafka.api;

import com.pehlione.kafka.auth.RegisterForm;
import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.model.User;
import com.pehlione.kafka.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterControllerTest {

    private UserService userService;
    private CommerceEventPublisher commerceEventPublisher;
    private RegisterController registerController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        commerceEventPublisher = mock(CommerceEventPublisher.class);
        registerController = new RegisterController(userService, commerceEventPublisher);
    }

    @Test
    void registerPageShouldPopulateFormAndLoggedInState() {
        Model model = new ExtendedModelMap();
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("alice", "pw", "ROLE_USER");

        String viewName = registerController.registerPage(model, authentication);

        assertThat(viewName).isEqualTo("register");
        assertThat(model.getAttribute("loggedIn")).isEqualTo(true);
        assertThat(model.getAttribute("registerForm")).isInstanceOf(RegisterForm.class);
    }

    @Test
    void doRegisterShouldPublishEventOnSuccess() {
        RegisterForm form = sampleRegisterForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "registerForm");
        Model model = new ExtendedModelMap();
        User user = User.builder().username("alice").email("alice@example.com").password("encoded").build();

        when(userService.register("alice", "alice@example.com", "secret123")).thenReturn(user);

        String viewName = registerController.doRegister(form, bindingResult, model, null);

        assertThat(viewName).isEqualTo("register");
        assertThat(model.getAttribute("successMessage")).isEqualTo("Kayıt başarılı: alice");
        assertThat(model.getAttribute("registerForm")).isInstanceOf(RegisterForm.class);
        verify(commerceEventPublisher).publishUserRegistered(user);
    }

    @Test
    void doRegisterShouldReturnSameViewWhenValidationFails() {
        RegisterForm form = sampleRegisterForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "registerForm");
        bindingResult.rejectValue("email", "invalid", "invalid");
        Model model = new ExtendedModelMap();

        String viewName = registerController.doRegister(form, bindingResult, model, null);

        assertThat(viewName).isEqualTo("register");
        assertThat(model.getAttribute("loggedIn")).isEqualTo(false);
        verify(userService, never()).register(form.getUsername(), form.getEmail(), form.getPassword());
        verify(commerceEventPublisher, never()).publishUserRegistered(any(User.class));
    }

    @Test
    void doRegisterShouldExposeErrorMessageWhenUsernameExists() {
        RegisterForm form = sampleRegisterForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "registerForm");
        Model model = new ExtendedModelMap();

        when(userService.register("alice", "alice@example.com", "secret123"))
                .thenThrow(new IllegalArgumentException("This username is already taken."));

        String viewName = registerController.doRegister(form, bindingResult, model, null);

        assertThat(viewName).isEqualTo("register");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("This username is already taken.");
        verify(commerceEventPublisher, never()).publishUserRegistered(any(User.class));
    }

    private RegisterForm sampleRegisterForm() {
        RegisterForm form = new RegisterForm();
        form.setUsername("alice");
        form.setEmail("alice@example.com");
        form.setPassword("secret123");
        return form;
    }
}
