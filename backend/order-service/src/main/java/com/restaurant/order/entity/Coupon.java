package com.restaurant.order.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "coupons")
@Data
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private double discountAmount;

    @Column(nullable = false)
    private int minOrdersRequired;

    private boolean active = true;
}