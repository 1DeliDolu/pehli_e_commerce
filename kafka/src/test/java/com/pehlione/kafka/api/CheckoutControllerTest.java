package com.pehlione.kafka.api;

import com.pehlione.kafka.checkout.CheckoutForm;
import com.pehlione.kafka.checkout.CheckoutPaymentMethod;
import com.pehlione.kafka.checkout.PaymentSimulationResult;
import com.pehlione.kafka.dto.CartItemView;
import com.pehlione.kafka.dto.MailJobMessage;
import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.messaging.MailQueueProducer;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.model.Order;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.model.User;
import com.pehlione.kafka.service.CartService;
import com.pehlione.kafka.service.OrderMailService;
import com.pehlione.kafka.service.OrderService;
import com.pehlione.kafka.service.PaymentSimulationService;
import com.pehlione.kafka.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutControllerTest {

    private CartService cartService;
    private CommerceEventPublisher commerceEventPublisher;
    private UserService userService;
    private OrderService orderService;
    private OrderMailService orderMailService;
    private MailQueueProducer mailQueueProducer;
    private PaymentSimulationService paymentSimulationService;
    private CheckoutController checkoutController;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        commerceEventPublisher = mock(CommerceEventPublisher.class);
        userService = mock(UserService.class);
        orderService = mock(OrderService.class);
        orderMailService = mock(OrderMailService.class);
        mailQueueProducer = mock(MailQueueProducer.class);
        paymentSimulationService = mock(PaymentSimulationService.class);
        checkoutController = new CheckoutController(
                cartService,
                commerceEventPublisher,
                userService,
                orderService,
                orderMailService,
                mailQueueProducer,
                paymentSimulationService
        );
    }

    @Test
    void checkoutShouldPopulateGuestFormWhenCartHasItems() {
        HttpSession session = mock(HttpSession.class);
        Model model = new ExtendedModelMap();

        when(cartService.isEmpty(session)).thenReturn(false);
        when(cartService.getItems(session)).thenReturn(List.of(sampleCartItem()));
        when(cartService.getSubtotal(session)).thenReturn(BigDecimal.valueOf(199.90));

        String viewName = checkoutController.checkout(model, session, null);

        assertThat(viewName).isEqualTo("checkout/index");
        assertThat(model.getAttribute("checkoutForm")).isInstanceOf(CheckoutForm.class);
        assertThat((CheckoutPaymentMethod[]) model.getAttribute("paymentMethods"))
                .containsExactly(CheckoutPaymentMethod.values());
        assertThat(model.getAttribute("checkoutCurrentStep")).isEqualTo(1);
    }

    @Test
    void completeCheckoutShouldSubmitGuestOrder() {
        HttpSession session = mock(HttpSession.class);
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        CheckoutForm form = sampleCheckoutForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "checkoutForm");
        MailJobMessage mailJobMessage = new MailJobMessage(
                "job-1",
                "Max Mustermann",
                "max@example.de",
                "subject",
                "body",
                1,
                BigDecimal.valueOf(199.90),
                "checkout.completed",
                0,
                LocalDateTime.now()
        );
        PaymentSimulationResult paymentSimulationResult = new PaymentSimulationResult(
                CheckoutPaymentMethod.PAYPAL,
                "APPROVED",
                "PP-TX-12345678",
                "PP-SIM-123456789012",
                "PayPal",
                "PayPal sandbox onayi simule edildi.",
                BigDecimal.valueOf(199.90),
                LocalDateTime.now(),
                true
        );
        Order order = Order.builder()
                .id(101L)
                .orderNumber("ORD-20260405-ABCDEFGH")
                .status("PLACED")
                .build();

        when(cartService.isEmpty(session)).thenReturn(false);
        when(cartService.getItems(session)).thenReturn(List.of(sampleCartItem()));
        when(cartService.getSubtotal(session)).thenReturn(BigDecimal.valueOf(199.90));
        when(paymentSimulationService.simulate(eq(CheckoutPaymentMethod.PAYPAL), eq(BigDecimal.valueOf(199.90)), any()))
                .thenReturn(paymentSimulationResult);
        when(orderService.createOrder(any(), any(), any(), any(), eq(CheckoutPaymentMethod.PAYPAL), eq(paymentSimulationResult), any(), eq(BigDecimal.valueOf(199.90))))
                .thenReturn(order);
        when(orderMailService.createCheckoutMailJob(any(), eq("ORD-20260405-ABCDEFGH"), any(), eq(CheckoutPaymentMethod.PAYPAL), eq(paymentSimulationResult), any(), eq(BigDecimal.valueOf(199.90))))
                .thenReturn(mailJobMessage);

        String viewName = checkoutController.completeCheckout(
                session,
                form,
                bindingResult,
                null,
                model,
                redirectAttributes
        );

        assertThat(viewName).isEqualTo("redirect:/products");
        verify(mailQueueProducer).enqueue(mailJobMessage);
        verify(commerceEventPublisher).publishMailRequested(mailJobMessage);
        verify(commerceEventPublisher).publishCheckoutCompleted(
                eq(session),
                eq(order),
                any(),
                eq(BigDecimal.valueOf(199.90)),
                any(),
                any(),
                eq(CheckoutPaymentMethod.PAYPAL),
                eq(paymentSimulationResult)
        );
        verify(cartService).clear(session);
        verify(userService, never()).updateCheckoutProfile(any(), any());
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("successMessage")
                .isEqualTo("Siparis olusturuldu: ORD-20260405-ABCDEFGH. Odeme simule edildi: PP-TX-12345678. Yontem: PayPal. Siparis ozeti e-posta kuyruguna alindi.");
    }

    @Test
    void completeCheckoutShouldPersistProfileForLoggedInUser() {
        HttpSession session = mock(HttpSession.class);
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        CheckoutForm form = sampleCheckoutForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "checkoutForm");
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("alice", "pw", "ROLE_USER");
        MailJobMessage mailJobMessage = new MailJobMessage(
                "job-2",
                "Max Mustermann",
                "max@example.de",
                "subject",
                "body",
                1,
                BigDecimal.valueOf(199.90),
                "checkout.completed",
                0,
                LocalDateTime.now()
        );
        PaymentSimulationResult paymentSimulationResult = new PaymentSimulationResult(
                CheckoutPaymentMethod.PAYPAL,
                "APPROVED",
                "PP-TX-87654321",
                "PP-SIM-210987654321",
                "PayPal",
                "PayPal sandbox onayi simule edildi.",
                BigDecimal.valueOf(199.90),
                LocalDateTime.now(),
                true
        );
        Order order = Order.builder()
                .id(102L)
                .orderNumber("ORD-20260405-HGFEDCBA")
                .status("PLACED")
                .build();
        User user = User.builder()
                .id(5L)
                .username("alice")
                .email("alice@example.de")
                .password("secret")
                .build();

        when(cartService.isEmpty(session)).thenReturn(false);
        when(cartService.getItems(session)).thenReturn(List.of(sampleCartItem()));
        when(cartService.getSubtotal(session)).thenReturn(BigDecimal.valueOf(199.90));
        when(userService.findRequiredByUsername("alice")).thenReturn(user);
        when(paymentSimulationService.simulate(eq(CheckoutPaymentMethod.PAYPAL), eq(BigDecimal.valueOf(199.90)), any()))
                .thenReturn(paymentSimulationResult);
        when(orderService.createOrder(any(), eq(user), any(), any(), eq(CheckoutPaymentMethod.PAYPAL), eq(paymentSimulationResult), any(), eq(BigDecimal.valueOf(199.90))))
                .thenReturn(order);
        when(orderMailService.createCheckoutMailJob(any(), eq("ORD-20260405-HGFEDCBA"), any(), eq(CheckoutPaymentMethod.PAYPAL), eq(paymentSimulationResult), any(), eq(BigDecimal.valueOf(199.90))))
                .thenReturn(mailJobMessage);

        checkoutController.completeCheckout(
                session,
                form,
                bindingResult,
                authentication,
                model,
                redirectAttributes
        );

        verify(userService).updateCheckoutProfile("alice", form);
    }

    @Test
    void completeCheckoutShouldStayOnCurrentStepWhenValidationFails() {
        HttpSession session = mock(HttpSession.class);
        Model model = new ExtendedModelMap();
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        CheckoutForm form = sampleCheckoutForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "checkoutForm");
        bindingResult.rejectValue("postalCode", "invalid", "invalid");

        when(cartService.isEmpty(session)).thenReturn(false);
        when(cartService.getItems(session)).thenReturn(List.of(sampleCartItem()));
        when(cartService.getSubtotal(session)).thenReturn(BigDecimal.valueOf(199.90));

        String viewName = checkoutController.completeCheckout(
                session,
                form,
                bindingResult,
                null,
                model,
                redirectAttributes
        );

        assertThat(viewName).isEqualTo("checkout/index");
        assertThat(model.getAttribute("checkoutCurrentStep")).isEqualTo(2);
        verify(mailQueueProducer, never()).enqueue(any());
        verify(cartService, never()).clear(session);
    }

    private CheckoutForm sampleCheckoutForm() {
        CheckoutForm form = new CheckoutForm();
        form.setFirstName("Max");
        form.setLastName("Mustermann");
        form.setEmail("max@example.de");
        form.setPhone("+49 1512 3456789");
        form.setCompany("Musterfirma GmbH");
        form.setStreet("Musterstrasse");
        form.setHouseNumber("12A");
        form.setAddressLine2("2. Etage");
        form.setPostalCode("10115");
        form.setCity("Berlin");
        form.setCountryCode("DE");
        form.setPaymentMethod(CheckoutPaymentMethod.PAYPAL);
        return form;
    }

    private CartItemView sampleCartItem() {
        Category category = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();
        Product product = Product.builder()
                .id(10L)
                .name("Mechanical Keyboard")
                .price(BigDecimal.valueOf(199.90))
                .category(category)
                .build();
        return new CartItemView(product, 1, BigDecimal.valueOf(199.90));
    }
}
