package com.pehlione.kafka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryForm {

    @NotBlank(message = "Category name is required.")
    @Size(max = 100, message = "Category name can be a maximum of 100 characters.")
    private String name;

    @Size(max = 500, message = "Description can be a maximum of 500 characters.")
    private String description;
}
