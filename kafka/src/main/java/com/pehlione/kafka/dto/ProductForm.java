package com.pehlione.kafka.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductForm {

    @NotBlank(message = "Product name is required.")
    @Size(max = 150, message = "Product name can be maximum 150 characters.")
    private String name;

    @Size(max = 1000, message = "Description can be maximum 1000 characters.")
    private String description;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.01", message = "Price must be 0.01 or greater.")
    private BigDecimal price;

    @NotNull(message = "You must select a category.")
    private Long categoryId;
}
