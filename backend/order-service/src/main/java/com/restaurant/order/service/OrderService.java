package com.restaurant.order.service;

import com.restaurant.order.dto.*;
import com.restaurant.order.entity.*;
import com.restaurant.order.exception.InvalidPincodeException;
import com.restaurant.order.exception.ResourceNotFoundException;
import com.restaurant.order.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponRepository couponRepository;
    private final ServiceableAreaRepository serviceableAreaRepository;
    private final CartService cartService;
    private final NotificationService notificationService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CouponRepository couponRepository,
                        ServiceableAreaRepository serviceableAreaRepository, CartService cartService,
                        NotificationService notificationService, CartRepository cartRepository,
                        CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.couponRepository = couponRepository;
        this.serviceableAreaRepository = serviceableAreaRepository;
        this.cartService = cartService;
        this.notificationService = notificationService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public OrderDto createOrder(Long userId, CheckoutRequestDto dto, String authenticatedEmail) {
        CartDto cart = cartService.getCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        ServiceableArea area = serviceableAreaRepository.findByPincode(dto.getPincode())
                .orElseThrow(() -> new InvalidPincodeException("Sorry, we do not deliver to this pincode"));

        double total = cart.getTotalAmount();
        double discount = 0;
        if (dto.getCouponCode() != null) {
            Coupon coupon = couponRepository.findByCode(dto.getCouponCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid coupon"));
            if (coupon.isActive()) {
                discount = coupon.getDiscountAmount();
            }
        }
        double finalAmount = total - discount;

        Order order = new Order();
        order.setUserId(userId);
        order.setUserEmail(authenticatedEmail);
        order.setTotalAmount(total);
        order.setFinalAmount(finalAmount);
        order.setDeliveryAddress(dto.getDeliveryAddress());
        order.setPhoneNumber(dto.getPhoneNumber());
        order.setPincode(dto.getPincode());
        order.setEstimatedDeliveryMinutes(area.getDeliveryTimeMinutes());
        order.setExpectedDeliveryTime(LocalDateTime.now().plusMinutes(area.getDeliveryTimeMinutes()));
        System.out.println("Received coupon code from request: " + dto.getCouponCode());
        order.setCouponCode(dto.getCouponCode());
        System.out.println("Coupon code set on order: " + order.getCouponCode());
        Order saved = orderRepository.save(order);

        // Save order items
        for (CartItemDto item : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(saved);
            orderItem.setMenuItemId(item.getMenuItemId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPriceAtAdd());
            orderItemRepository.save(orderItem);
        }

        // Flush to make sure items are queryable before email
        orderItemRepository.flush();

        // Send email with items visible
        notificationService.sendOrderConfirmation(saved);

        // Clear cart after email
        cartService.clearCart(userId);

        return toOrderDto(saved);
    }

    public OrderDto getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        return toOrderDto(order);
    }

    public List<OrderDto> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream().map(this::toOrderDto).collect(Collectors.toList());
    }

    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::toOrderDto).collect(Collectors.toList());
    }

    public OrderDto updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        notificationService.sendOrderUpdateEmail(updated);
        return toOrderDto(updated);
    }

    public double getDailyRevenue() {
        return orderRepository.getDailyRevenue();
    }

    private OrderDto toOrderDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setFinalAmount(order.getFinalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setPhoneNumber(order.getPhoneNumber());
        dto.setPincode(order.getPincode());
        dto.setEstimatedDeliveryMinutes(order.getEstimatedDeliveryMinutes());
        dto.setExpectedDeliveryTime(order.getExpectedDeliveryTime());
        dto.setCouponCode(order.getCouponCode());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // FIXED: Use correct query
        List<OrderItem> items = orderItemRepository.getOrderItemsByOrderId(order.getId());
        dto.setItems(items.stream().map(this::toOrderItemDto).collect(Collectors.toList()));

        return dto;
    }

    private OrderItemDto toOrderItemDto(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setMenuItemId(item.getMenuItemId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }
}