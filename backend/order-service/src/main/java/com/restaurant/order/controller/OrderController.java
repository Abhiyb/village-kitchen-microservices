package com.restaurant.order.controller;

import com.restaurant.order.config.UserPrincipal;  // ← Import this (create if missing)
import com.restaurant.order.dto.*;
import com.restaurant.order.service.CartService;
import com.restaurant.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;

    public OrderController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    // User cart
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/user/cart")
    public ResponseEntity<ApiResponseDto<CartDto>> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            throw new IllegalStateException("User ID not found in token");
        }
        Long userId = principal.getUserId();
        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponseDto.success(cart));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/user/cart/items")
    public ResponseEntity<ApiResponseDto<CartItemDto>> addItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CartItemDto dto) {

        if (principal == null || principal.getUserId() == null) {
            throw new IllegalStateException("User ID not found in token");
        }
        Long userId = principal.getUserId();
        CartItemDto added = cartService.addItem(userId, dto);
        return ResponseEntity.ok(ApiResponseDto.success(added, "Item added to cart"));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/user/cart/items/{itemId}")
    public ResponseEntity<ApiResponseDto<Void>> removeItem(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long itemId) {

        if (principal == null || principal.getUserId() == null) {
            throw new IllegalStateException("User ID not found in token");
        }
        Long userId = principal.getUserId();
        cartService.removeItem(userId, itemId);
        return ResponseEntity.ok(ApiResponseDto.success("Item removed from cart"));
    }

    // Checkout & Order
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/user/checkout")
    public ResponseEntity<ApiResponseDto<OrderDto>> checkout(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CheckoutRequestDto dto) {

        if (principal == null || principal.getUserId() == null) {
            throw new IllegalStateException("User ID not found in token");
        }
        Long userId = principal.getUserId();
        String email = principal.getEmail();  // For order.userEmail

        OrderDto order = orderService.createOrder(userId, dto, email);
        return ResponseEntity.ok(ApiResponseDto.success(order, "Order created. Proceed to payment."));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/user/orders")
    public ResponseEntity<ApiResponseDto<List<OrderDto>>> getUserOrders(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            throw new IllegalStateException("User ID not found in token");
        }
        Long userId = principal.getUserId();
        List<OrderDto> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(ApiResponseDto.success(orders));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/user/orders/{id}")
    public ResponseEntity<ApiResponseDto<OrderDto>> getOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {

        if (principal == null || principal.getUserId() == null) {
            throw new IllegalStateException("User ID not found in token");
        }
        Long userId = principal.getUserId();
        OrderDto order = orderService.getOrder(userId, id);
        return ResponseEntity.ok(ApiResponseDto.success(order));
    }

    // Admin endpoints (no change needed here, they don't use userId)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders")
    public ResponseEntity<ApiResponseDto<List<OrderDto>>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponseDto.success(orders));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/orders/{id}/status")
    public ResponseEntity<ApiResponseDto<OrderDto>> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequestDto dto) {
        OrderDto updated = orderService.updateStatus(id, dto.getNewStatus());
        return ResponseEntity.ok(ApiResponseDto.success(updated, "Status updated"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/revenue/daily")
    public ResponseEntity<ApiResponseDto<Double>> getDailyRevenue() {
        double revenue = orderService.getDailyRevenue();
        return ResponseEntity.ok(ApiResponseDto.success(revenue, "Daily revenue"));
    }
}