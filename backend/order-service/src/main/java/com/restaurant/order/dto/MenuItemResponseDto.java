package com.restaurant.order.dto;

import lombok.Data;

@Data
public class MenuItemResponseDto {
    private boolean success;
    private MenuItemDto data;  // ← inner data object
    private String message;
}