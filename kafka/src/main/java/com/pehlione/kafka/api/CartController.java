package com.pehlione.kafka.api;

import com.pehlione.kafka.dto.CartActionResponse;
import com.pehlione.kafka.dto.CartItemView;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.service.ProductService;
import com.pehlione.kafka.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final CommerceEventPublisher commerceEventPublisher;

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        model.addAttribute("cartItems", cartService.getItems(session));
        model.addAttribute("cartSubtotal", cartService.getSubtotal(session));
        return "cart/index";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "/cart") String redirectTo,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        CartActionResponse response = addToCartInternal(productId, session);
        redirectAttributes.addFlashAttribute("successMessage", response.message());
        return "redirect:" + redirectTo;
    }

    @PostMapping("/api/cart/add")
    @ResponseBody
    public CartActionResponse addToCartAjax(@RequestParam Long productId, HttpSession session) {
        return addToCartInternal(productId, session);
    }

    @PostMapping("/cart/{productId}/quantity")
    public String updateQuantity(@PathVariable Long productId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        CartActionResponse response = updateQuantityInternal(productId, quantity, session);
        redirectAttributes.addFlashAttribute("successMessage", response.message());
        return "redirect:/cart";
    }

    @PostMapping("/api/cart/{productId}/quantity")
    @ResponseBody
    public CartActionResponse updateQuantityAjax(@PathVariable Long productId,
                                                 @RequestParam int quantity,
                                                 HttpSession session) {
        return updateQuantityInternal(productId, quantity, session);
    }

    @PostMapping("/cart/{productId}/remove")
    public String removeFromCart(@PathVariable Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        CartActionResponse response = removeFromCartInternal(productId, session);
        redirectAttributes.addFlashAttribute("successMessage", response.message());
        return "redirect:/cart";
    }

    @PostMapping("/api/cart/{productId}/remove")
    @ResponseBody
    public CartActionResponse removeFromCartAjax(@PathVariable Long productId, HttpSession session) {
        return removeFromCartInternal(productId, session);
    }

    private CartActionResponse addToCartInternal(Long productId, HttpSession session) {
        cartService.addProduct(session, productId);
        Product product = productService.findById(productId);
        int quantity = findItem(session, productId)
                .map(CartItemView::quantity)
                .orElse(1);
        commerceEventPublisher.publishCartEvent("cart.added", session, productId, product.getName(), quantity);
        return buildResponse("Urun sepete eklendi.", session, productId);
    }

    private CartActionResponse updateQuantityInternal(Long productId, int quantity, HttpSession session) {
        cartService.updateQuantity(session, productId, quantity);
        Product product = productService.findById(productId);
        int updatedQuantity = Math.max(quantity, 0);
        commerceEventPublisher.publishCartEvent("cart.updated", session, productId, product.getName(), updatedQuantity);
        return buildResponse("Sepet guncellendi.", session, productId);
    }

    private CartActionResponse removeFromCartInternal(Long productId, HttpSession session) {
        Product product = productService.findById(productId);
        cartService.removeProduct(session, productId);
        commerceEventPublisher.publishCartEvent("cart.removed", session, productId, product.getName(), 0);
        return buildResponse("Urun sepetten kaldirildi.", session, productId);
    }

    private java.util.Optional<CartItemView> findItem(HttpSession session, Long productId) {
        return cartService.getItems(session).stream()
                .filter(item -> item.product().getId().equals(productId))
                .findFirst();
    }

    private CartActionResponse buildResponse(String message, HttpSession session, Long productId) {
        java.util.Optional<CartItemView> item = findItem(session, productId);
        return new CartActionResponse(
                message,
                cartService.getItemCount(session),
                cartService.isEmpty(session),
                productId,
                item.map(CartItemView::quantity).orElse(0),
                item.map(CartItemView::lineTotal).map(this::formatCurrency).orElse("0.00 EUR"),
                formatCurrency(cartService.getSubtotal(session))
        );
    }

    private String formatCurrency(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " EUR";
    }
}
