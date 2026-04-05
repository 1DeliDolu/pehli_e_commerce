package com.pehlione.kafka.service;

import com.pehlione.kafka.dto.CartItemView;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CartServiceTest {

    private ProductService productService;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        productService = mock(ProductService.class);
        cartService = new CartService(productService);
    }

    @Test
    void addProductShouldIncreaseItemCount() {
        MockHttpSession session = new MockHttpSession();

        cartService.addProduct(session, 1L);
        cartService.addProduct(session, 1L);

        assertThat(cartService.getItemCount(session)).isEqualTo(2);
        assertThat(cartService.isEmpty(session)).isFalse();
    }

    @Test
    void getItemsShouldBuildLineTotalsFromProductPrices() {
        MockHttpSession session = new MockHttpSession();
        Product product = sampleProduct(1L, "Keyboard", "129.90");

        cartService.addProduct(session, 1L);
        cartService.addProduct(session, 1L);
        when(productService.findById(1L)).thenReturn(product);

        List<CartItemView> items = cartService.getItems(session);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).quantity()).isEqualTo(2);
        assertThat(items.get(0).lineTotal()).isEqualByComparingTo("259.80");
        assertThat(cartService.getSubtotal(session)).isEqualByComparingTo("259.80");
    }

    @Test
    void updateQuantityShouldRemoveProductWhenQuantityIsZero() {
        MockHttpSession session = new MockHttpSession();

        cartService.addProduct(session, 1L);
        cartService.updateQuantity(session, 1L, 0);

        assertThat(cartService.isEmpty(session)).isTrue();
        assertThat(cartService.getItemCount(session)).isZero();
    }

    @Test
    void clearShouldRemoveCartFromSession() {
        MockHttpSession session = new MockHttpSession();

        cartService.addProduct(session, 1L);
        cartService.clear(session);

        assertThat(cartService.isEmpty(session)).isTrue();
        assertThat(cartService.getItemCount(session)).isZero();
    }

    private Product sampleProduct(Long id, String name, String price) {
        return Product.builder()
                .id(id)
                .name(name)
                .price(new BigDecimal(price))
                .category(Category.builder().id(10L).name("Electronics").build())
                .build();
    }
}
