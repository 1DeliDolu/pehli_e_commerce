package com.pehlione.kafka.service;

import com.pehlione.kafka.dto.CartItemView;
import com.pehlione.kafka.model.Product;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String CART_SESSION_KEY = "cart";

    private final ProductService productService;

    public void addProduct(HttpSession session, Long productId) {
        Map<Long, Integer> cart = getOrCreateCart(session);
        cart.merge(productId, 1, Integer::sum);
    }

    public void updateQuantity(HttpSession session, Long productId, int quantity) {
        Map<Long, Integer> cart = getOrCreateCart(session);
        if (quantity <= 0) {
            cart.remove(productId);
            return;
        }
        cart.put(productId, quantity);
    }

    public void removeProduct(HttpSession session, Long productId) {
        getOrCreateCart(session).remove(productId);
    }

    public List<CartItemView> getItems(HttpSession session) {
        List<CartItemView> items = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : getOrCreateCart(session).entrySet()) {
            Product product = productService.findById(entry.getKey());
            int quantity = entry.getValue();
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            items.add(new CartItemView(product, quantity, lineTotal));
        }

        return items;
    }

    public int getItemCount(HttpSession session) {
        return getOrCreateCart(session).values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public BigDecimal getSubtotal(HttpSession session) {
        return getItems(session).stream()
                .map(CartItemView::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty(HttpSession session) {
        return getOrCreateCart(session).isEmpty();
    }

    public void clear(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getOrCreateCart(HttpSession session) {
        Object cart = session.getAttribute(CART_SESSION_KEY);
        if (cart instanceof Map<?, ?> existingCart) {
            return (Map<Long, Integer>) existingCart;
        }

        Map<Long, Integer> newCart = new LinkedHashMap<>();
        session.setAttribute(CART_SESSION_KEY, newCart);
        return newCart;
    }
}
