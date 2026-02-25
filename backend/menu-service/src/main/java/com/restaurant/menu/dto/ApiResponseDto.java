package com.restaurant.menu.dto;

import lombok.Data;

@Data
public class ApiResponseDto<T> {

    private boolean success;
    private T data;
    private String message;

    public static <T> ApiResponseDto<T> success(T data, String message) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.success = true;
        response.data = data;
        response.message = message;
        return response;
    }

    public static <T> ApiResponseDto<T> success(T data) {
        return success(data, "Operation successful");
    }

    public static <T> ApiResponseDto<T> success(String message) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.success = true;
        response.message = message;
        return response;
    }

    public static <T> ApiResponseDto<T> error(String message) {
        ApiResponseDto<T> response = new ApiResponseDto<>();
        response.success = false;
        response.message = message;
        return response;
    }
}