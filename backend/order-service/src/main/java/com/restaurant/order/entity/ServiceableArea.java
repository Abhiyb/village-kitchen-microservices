package com.restaurant.order.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "serviceable_areas")
@Data
public class ServiceableArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pincode;

    private String areaName;

    @Column(nullable = false)
    private int deliveryTimeMinutes;

    @Column(nullable = false)
    private double deliveryFee;

    public ServiceableArea(String pincode, String areaName, int deliveryTimeMinutes, double deliveryFee) {
        this.pincode = pincode;
        this.areaName = areaName;
        this.deliveryTimeMinutes = deliveryTimeMinutes;
        this.deliveryFee = deliveryFee;
    }

    public ServiceableArea() {}
}