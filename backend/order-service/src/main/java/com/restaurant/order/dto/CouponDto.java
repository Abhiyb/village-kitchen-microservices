package com.restaurant.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CouponDto {
    private Long id;
    @NotBlank(message = "Code is required")
    private String code;
    @Min(value = 0, message = "Discount amount must be positive")
    private double discountAmount;
    @Min(value = 0, message = "Min orders required must be non-negative")
    private int minOrdersRequired;
    private boolean active = true;
}