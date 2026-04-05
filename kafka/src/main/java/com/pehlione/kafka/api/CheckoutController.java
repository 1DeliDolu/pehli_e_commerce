package com.pehlione.kafka.api;

import com.pehlione.kafka.checkout.CheckoutAddressDetails;
import com.pehlione.kafka.checkout.CheckoutCustomerDetails;
import com.pehlione.kafka.checkout.CheckoutForm;
import com.pehlione.kafka.checkout.CheckoutPaymentMethod;
import com.pehlione.kafka.checkout.PaymentSimulationResult;
import com.pehlione.kafka.dto.CartItemView;
import com.pehlione.kafka.dto.MailJobMessage;
import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.messaging.MailQueueProducer;
import com.pehlione.kafka.model.Order;
import com.pehlione.kafka.model.User;
import com.pehlione.kafka.service.CartService;
import com.pehlione.kafka.service.OrderService;
import com.pehlione.kafka.service.OrderMailService;
import com.pehlione.kafka.service.PaymentSimulationService;
import com.pehlione.kafka.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final CommerceEventPublisher commerceEventPublisher;
    private final UserService userService;
    private final OrderService orderService;
    private final OrderMailService orderMailService;
    private final MailQueueProducer mailQueueProducer;
    private final PaymentSimulationService paymentSimulationService;

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session, Authentication authentication) {
        if (cartService.isEmpty(session)) {
            return "redirect:/cart";
        }

        populateCheckoutModel(model, session, buildCheckoutForm(authentication), 1);
        return "checkout/index";
    }

    @PostMapping("/checkout")
    public String completeCheckout(HttpSession session,
                                   @Valid @ModelAttribute("checkoutForm") CheckoutForm checkoutForm,
                                   BindingResult bindingResult,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (cartService.isEmpty(session)) {
            return "redirect:/cart";
        }

        java.util.List<CartItemView> cartItems = cartService.getItems(session);
        java.math.BigDecimal subtotal = cartService.getSubtotal(session);
        boolean loggedIn = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName());

        if (bindingResult.hasErrors()) {
            populateCheckoutModel(model, session, checkoutForm, resolveCurrentStep(bindingResult));
            return "checkout/index";
        }

        CheckoutCustomerDetails customerDetails = checkoutForm.toCustomerDetails(loggedIn);
        CheckoutAddressDetails shippingAddress = checkoutForm.toAddressDetails();
        User user = null;

        if (loggedIn) {
            userService.updateCheckoutProfile(authentication.getName(), checkoutForm);
            user = userService.findRequiredByUsername(authentication.getName());
        }

        PaymentSimulationResult paymentSimulationResult = paymentSimulationService.simulate(
                checkoutForm.getPaymentMethod(),
                subtotal,
                customerDetails
        );

        Order order = orderService.createOrder(
                session.getId(),
                user,
                customerDetails,
                shippingAddress,
                checkoutForm.getPaymentMethod(),
                paymentSimulationResult,
                cartItems,
                subtotal
        );

        MailJobMessage mailJob = orderMailService.createCheckoutMailJob(
                customerDetails,
                order.getOrderNumber(),
                shippingAddress,
                checkoutForm.getPaymentMethod(),
                paymentSimulationResult,
                cartItems,
                subtotal
        );
        mailQueueProducer.enqueue(mailJob);
        commerceEventPublisher.publishMailRequested(mailJob);
        commerceEventPublisher.publishCheckoutCompleted(
                session,
                order,
                cartItems,
                subtotal,
                customerDetails,
                shippingAddress,
                checkoutForm.getPaymentMethod(),
                paymentSimulationResult
        );
        cartService.clear(session);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Siparis olusturuldu: " + order.getOrderNumber()
                        + ". Odeme simule edildi: " + paymentSimulationResult.transactionId()
                        + ". Yontem: " + checkoutForm.getPaymentMethod().getDisplayName()
                        + ". Siparis ozeti e-posta kuyruguna alindi."
        );
        return "redirect:/products";
    }

    private CheckoutForm buildCheckoutForm(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return new CheckoutForm();
        }
        return CheckoutForm.fromUser(userService.findRequiredByUsername(authentication.getName()));
    }

    private void populateCheckoutModel(Model model, HttpSession session, CheckoutForm checkoutForm, int currentStep) {
        model.addAttribute("cartItems", cartService.getItems(session));
        model.addAttribute("cartSubtotal", cartService.getSubtotal(session));
        model.addAttribute("checkoutForm", checkoutForm);
        model.addAttribute("paymentMethods", CheckoutPaymentMethod.values());
        model.addAttribute("checkoutCurrentStep", currentStep);
    }

    private int resolveCurrentStep(BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors("paymentMethod")) {
            return 3;
        }
        if (bindingResult.hasFieldErrors("street")
                || bindingResult.hasFieldErrors("houseNumber")
                || bindingResult.hasFieldErrors("addressLine2")
                || bindingResult.hasFieldErrors("postalCode")
                || bindingResult.hasFieldErrors("city")
                || bindingResult.hasFieldErrors("countryCode")) {
            return 2;
        }
        return 1;
    }
}
