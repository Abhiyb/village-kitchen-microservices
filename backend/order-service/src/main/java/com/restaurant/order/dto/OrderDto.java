package com.restaurant.order.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private double totalAmount;
    private double finalAmount;
    private String status;
    private String paymentStatus;
    private String deliveryAddress;
    private String phoneNumber;
    private String pincode;
    private int estimatedDeliveryMinutes;
    private LocalDateTime expectedDeliveryTime;
    private String couponCode;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}