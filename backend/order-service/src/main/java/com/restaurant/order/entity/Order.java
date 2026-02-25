package com.restaurant.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private double finalAmount;

    @Column(nullable = false)
    private String status = "CREATED";

    @Column(nullable = false)
    private String paymentStatus = "PENDING";

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String pincode;

    private int estimatedDeliveryMinutes;

    private LocalDateTime expectedDeliveryTime;

    private String couponCode;

    @Column(nullable = false)
    private String userEmail;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();


    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}