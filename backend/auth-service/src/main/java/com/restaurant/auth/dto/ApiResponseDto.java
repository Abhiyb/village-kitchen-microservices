package com.restaurant.auth.dto;

import lombok.Data;

@Data
public class ApiResponseDto<T> {

    private boolean success;
    private T data;
    private String message;

    // Success with data
    public static <T> ApiResponseDto<T> success(T data, String message) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.success = true;
        response.data = data;
        response.message = message;
        return response;
    }

    // Success without data (e.g. "OTP sent")
    public static <T> ApiResponseDto<T> success(String message) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.success = true;
        response.message = message;
        return response;
    }

    // Error (data = null)
    public static <T> ApiResponseDto<T> error(String message) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.success = false;
        response.message = message;
        return response;
    }
}