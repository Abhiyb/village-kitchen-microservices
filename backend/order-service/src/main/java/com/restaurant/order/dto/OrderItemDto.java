package com.restaurant.order.dto;

import lombok.Data;

@Data
public class OrderItemDto {
    private Long id;
    private Long menuItemId;
    private int quantity;
    private double price;
}