package com.restaurant.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequestDto {
    @NotBlank(message = "New status is required")
    private String newStatus;
}