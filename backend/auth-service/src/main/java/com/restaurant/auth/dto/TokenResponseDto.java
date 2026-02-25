package com.restaurant.auth.dto;

import lombok.Data;

@Data
public class TokenResponseDto {
    private String token;
    private String message = "Login successful";
}