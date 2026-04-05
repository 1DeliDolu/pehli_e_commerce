package com.pehlione.kafka.api;

import com.pehlione.kafka.dto.CartActionResponse;
import com.pehlione.kafka.dto.CartItemView;
import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.service.CartService;
import com.pehlione.kafka.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartControllerTest {

    private CartService cartService;
    private ProductService productService;
    private CommerceEventPublisher commerceEventPublisher;
    private CartController cartController;

    @BeforeEach
    void setUp() {
        cartService = mock(CartService.class);
        productService = mock(ProductService.class);
        commerceEventPublisher = mock(CommerceEventPublisher.class);
        cartController = new CartController(cartService, productService, commerceEventPublisher);
    }

    @Test
    void cartShouldPopulateItemsAndSubtotal() {
        HttpSession session = mock(HttpSession.class);
        Model model = new ExtendedModelMap();
        List<CartItemView> items = List.of(sampleItem(1L, "Keyboard", 2, "259.80"));

        when(cartService.getItems(session)).thenReturn(items);
        when(cartService.getSubtotal(session)).thenReturn(BigDecimal.valueOf(259.80));

        String viewName = cartController.cart(model, session);

        assertThat(viewName).isEqualTo("cart/index");
        assertThat(model.getAttribute("cartItems")).isEqualTo(items);
        assertThat(model.getAttribute("cartSubtotal")).isEqualTo(BigDecimal.valueOf(259.80));
    }

    @Test
    void addToCartAjaxShouldReturnUpdatedCartState() {
        HttpSession session = mock(HttpSession.class);
        Product product = sampleProduct(1L, "Keyboard");
        List<CartItemView> items = List.of(sampleItem(1L, "Keyboard", 2, "259.80"));

        when(productService.findById(1L)).thenReturn(product);
        when(cartService.getItems(session)).thenReturn(items);
        when(cartService.getItemCount(session)).thenReturn(2);
        when(cartService.isEmpty(session)).thenReturn(false);
        when(cartService.getSubtotal(session)).thenReturn(BigDecimal.valueOf(259.80));

        CartActionResponse response = cartController.addToCartAjax(1L, session);

        assertThat(response.message()).isEqualTo("Urun sepete eklendi.");
        assertThat(response.cartItemCount()).isEqualTo(2);
        assertThat(response.productQuantity()).isEqualTo(2);
        assertThat(response.productLineTotal()).isEqualTo("259.80 EUR");
        verify(cartService).addProduct(session, 1L);
        verify(commerceEventPublisher).publishCartEvent("cart.added", session, 1L, "Keyboard", 2);
    }

    @Test
    void updateQuantityAjaxShouldReturnFormattedTotals() {
        HttpSession session = mock(HttpSession.class);
        Product product = sampleProduct(1L, "Keyboard");
        List<CartItemView> items = List.of(sampleItem(1L, "Keyboard", 3, "389.70"));

        when(productService.findById(1L)).thenReturn(product);
        when(cartService.getItems(session)).thenReturn(items);
        when(cartService.getItemCount(session)).thenReturn(3);
        when(cartService.isEmpty(session)).thenReturn(false);
        when(cartService.getSubtotal(session)).thenReturn(BigDecimal.valueOf(389.70));

        CartActionResponse response = cartController.updateQuantityAjax(1L, 3, session);

        assertThat(response.message()).isEqualTo("Sepet guncellendi.");
        assertThat(response.productQuantity()).isEqualTo(3);
        assertThat(response.cartSubtotal()).isEqualTo("389.70 EUR");
        verify(cartService).updateQuantity(session, 1L, 3);
        verify(commerceEventPublisher).publishCartEvent("cart.updated", session, 1L, "Keyboard", 3);
    }

    @Test
    void removeFromCartShouldRedirectWithFlashMessage() {
        HttpSession session = mock(HttpSession.class);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();
        Product product = sampleProduct(1L, "Keyboard");

        when(productService.findById(1L)).thenReturn(product);
        when(cartService.getItems(session)).thenReturn(List.of());
        when(cartService.getItemCount(session)).thenReturn(0);
        when(cartService.isEmpty(session)).thenReturn(true);
        when(cartService.getSubtotal(session)).thenReturn(BigDecimal.ZERO);

        String viewName = cartController.removeFromCart(1L, session, redirectAttributes);

        assertThat(viewName).isEqualTo("redirect:/cart");
        assertThat(redirectAttributes.getFlashAttributes())
                .extractingByKey("successMessage")
                .isEqualTo("Urun sepetten kaldirildi.");
        verify(cartService).removeProduct(session, 1L);
        verify(commerceEventPublisher).publishCartEvent("cart.removed", session, 1L, "Keyboard", 0);
    }

    private CartItemView sampleItem(Long id, String name, int quantity, String lineTotal) {
        return new CartItemView(sampleProduct(id, name), quantity, new BigDecimal(lineTotal));
    }

    private Product sampleProduct(Long id, String name) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(BigDecimal.valueOf(129.90))
                .category(Category.builder().id(10L).name("Electronics").build())
                .build();
    }
}
