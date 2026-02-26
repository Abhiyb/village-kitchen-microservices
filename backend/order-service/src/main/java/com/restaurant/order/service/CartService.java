package com.restaurant.order.service;

import com.restaurant.order.dto.CartDto;
import com.restaurant.order.dto.CartItemDto;
import com.restaurant.order.dto.MenuItemDto;
import com.restaurant.order.dto.MenuItemResponseDto;
import com.restaurant.order.entity.Cart;
import com.restaurant.order.entity.CartItem;
import com.restaurant.order.exception.ResourceNotFoundException;
import com.restaurant.order.repository.CartItemRepository;
import com.restaurant.order.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestTemplate restTemplate;

    @Value("${menu.service.url}")
    private String menuServiceUrl;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, RestTemplate restTemplate) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.restTemplate = restTemplate;
    }

    public CartDto getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
        List<CartItem> items = cartItemRepository.findAll();  // TODO: Filter by cart.id (add method)
        // For simplicity, assume one cart per user, query items by cart

        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setUserId(userId);
        dto.setItems(items.stream().map(this::toCartItemDto).collect(Collectors.toList()));
        dto.setTotalAmount(calculateTotal(items));
        return dto;
    }

    public CartItemDto addItem(Long userId, CartItemDto dto) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        String url = menuServiceUrl + "/" + dto.getMenuItemId();
        System.out.println("[CART PRICE FETCH] Calling: " + url);

        try {
            MenuItemResponseDto response = restTemplate.getForObject(url, MenuItemResponseDto.class);
            if (response == null || !response.isSuccess() || response.getData() == null) {
                throw new ResourceNotFoundException("Menu item not found or invalid response");
            }

            double price = response.getData().getPrice();
            System.out.println("[CART PRICE FETCH] Success - Price: " + price);

            CartItem item = new CartItem();
            item.setCart(cart);
            item.setMenuItemId(dto.getMenuItemId());
            item.setQuantity(dto.getQuantity());
            item.setPriceAtAdd(price);

            CartItem saved = cartItemRepository.save(item);

            dto.setId(saved.getId());
            dto.setPriceAtAdd(price);
            return dto;
        } catch (Exception e) {
            System.err.println("[CART PRICE FETCH] Failed for item " + dto.getMenuItemId() + ": " + e.getMessage());
            e.printStackTrace();
            throw new ResourceNotFoundException("Failed to fetch price from Menu Service");
        }
    }

    public void removeItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        // Check if item belongs to cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Cart item not found in your cart");
        }
        cartItemRepository.delete(item);
    }
    // In CartService.java
    @Transactional  // Ensures delete runs in transaction
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        // Delete all items
        cartItemRepository.deleteAllByCartId(cart.getId());

        // Optional: delete the cart entity itself if empty
        if (cartItemRepository.countByCartId(cart.getId()) == 0) {
            cartRepository.delete(cart);
        }

        System.out.println("Cart cleared for user: " + userId);
    }
    private Cart createCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    private double calculateTotal(List<CartItem> items) {
        return items.stream().mapToDouble(i -> i.getQuantity() * i.getPriceAtAdd()).sum();
    }

    private CartItemDto toCartItemDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setMenuItemId(item.getMenuItemId());
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtAdd(item.getPriceAtAdd());
        return dto;
    }
}