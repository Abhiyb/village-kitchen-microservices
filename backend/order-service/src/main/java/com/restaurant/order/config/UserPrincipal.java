package com.restaurant.order.config;

import lombok.Data;

@Data
public class UserPrincipal {
    private final String email;
    private final Long userId;
    private final String role;

    public UserPrincipal(String email, Long userId, String role) {
        this.email = email;
        this.userId = userId;
        this.role = role;
    }
}
