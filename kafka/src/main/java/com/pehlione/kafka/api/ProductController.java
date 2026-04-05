package com.pehlione.kafka.api;

import com.pehlione.kafka.messaging.CommerceEventPublisher;
import com.pehlione.kafka.model.Category;
import com.pehlione.kafka.model.Product;
import com.pehlione.kafka.service.CategoryService;
import com.pehlione.kafka.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CommerceEventPublisher commerceEventPublisher;

    @GetMapping("/products")
    public String products(@RequestParam(required = false) Long categoryId,
                           Model model,
                           HttpSession session) {
        if (categoryId != null) {
            Category selectedCategory = categoryService.findById(categoryId);
            commerceEventPublisher.publishCategoryViewed(session, selectedCategory);
            model.addAttribute("selectedCategory", selectedCategory);
            model.addAttribute("products", productService.findAllByCategoryId(categoryId));
        } else {
            model.addAttribute("products", productService.findAll());
        }
        return "products/index";
    }

    @GetMapping("/products/{id}")
    public String productDetails(@PathVariable Long id, Model model, HttpSession session) {
        Product product = productService.findById(id);
        commerceEventPublisher.publishProductViewed(session, product);
        model.addAttribute("product", product);
        model.addAttribute("productImages", buildProductImages(product));
        return "products/detail";
    }

    private List<String> buildProductImages(Product product) {
        return List.of(
                productImageUrl(product.getId(), 1),
                productImageUrl(product.getId(), 2),
                productImageUrl(product.getId(), 3),
                productImageUrl(product.getId(), 4)
        );
    }

    private String productImageUrl(Long productId, int imageIndex) {
        return "https://picsum.photos/seed/kafka-product-" + productId + "-" + imageIndex + "/1200/1400";
    }
}
