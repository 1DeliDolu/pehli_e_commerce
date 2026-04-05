package com.pehlione.kafka.dto;

import com.pehlione.kafka.model.Product;

import java.math.BigDecimal;

public record CartItemView(Product product, int quantity, BigDecimal lineTotal) {
}
