package com.restaurant.order.dto;

import lombok.Data;

@Data
public class ServiceableAreaDto {
    private Long id;
    private String pincode;
    private String areaName;
    private int deliveryTimeMinutes;
    private double deliveryFee;
}