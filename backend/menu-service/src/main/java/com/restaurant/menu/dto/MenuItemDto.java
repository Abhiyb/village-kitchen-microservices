package com.restaurant.menu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MenuItemDto {

    private Long id;

    private Long categoryId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @Min(value = 0, message = "Price must be positive")
    private double price;

    private boolean available = true;

    private String imageUrl;

    private int preparationTimeMinutes = 0;
}