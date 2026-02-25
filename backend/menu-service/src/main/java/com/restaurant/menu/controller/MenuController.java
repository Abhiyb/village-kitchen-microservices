package com.restaurant.menu.controller;

import com.restaurant.menu.dto.ApiResponseDto;
import com.restaurant.menu.dto.CategoryDto;
import com.restaurant.menu.dto.MenuItemDto;
import com.restaurant.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // Public endpoints
    @GetMapping("/public/categories")
    public ResponseEntity<ApiResponseDto<List<CategoryDto>>> getAllCategories() {
        List<CategoryDto> categories = menuService.getAllCategories();
        return ResponseEntity.ok(ApiResponseDto.success(categories));
    }

    @GetMapping("/public/items")
    public ResponseEntity<ApiResponseDto<List<MenuItemDto>>> getAllItems(@RequestParam(required = false) String type) {
        List<MenuItemDto> items = menuService.getAllMenuItems(type);
        return ResponseEntity.ok(ApiResponseDto.success(items));
    }

    @GetMapping("/public/items/{id}")
    public ResponseEntity<ApiResponseDto<MenuItemDto>> getItemById(@PathVariable Long id) {
        MenuItemDto item = menuService.getMenuItemById(id);
        return ResponseEntity.ok(ApiResponseDto.success(item));
    }

    // Admin endpoints
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories")
    public ResponseEntity<ApiResponseDto<CategoryDto>> createCategory(@Valid @RequestBody CategoryDto dto) {
        CategoryDto created = menuService.createCategory(dto);
        return ResponseEntity.ok(ApiResponseDto.success(created, "Category created"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/categories/{id}")
    public ResponseEntity<ApiResponseDto<CategoryDto>> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        CategoryDto updated = menuService.updateCategory(id, dto);
        return ResponseEntity.ok(ApiResponseDto.success(updated, "Category updated"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteCategory(@PathVariable Long id) {
        menuService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponseDto.success("Category deleted"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/items")
    public ResponseEntity<ApiResponseDto<MenuItemDto>> createItem(@Valid @RequestBody MenuItemDto dto) {
        MenuItemDto created = menuService.createMenuItem(dto);
        return ResponseEntity.ok(ApiResponseDto.success(created, "Menu item created"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/items/{id}")
    public ResponseEntity<ApiResponseDto<MenuItemDto>> updateItem(@PathVariable Long id, @Valid @RequestBody MenuItemDto dto) {
        MenuItemDto updated = menuService.updateMenuItem(id, dto);
        return ResponseEntity.ok(ApiResponseDto.success(updated, "Menu item updated"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/items/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponseDto.success("Menu item deleted"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/items/{id}/availability")
    public ResponseEntity<ApiResponseDto<Void>> toggleAvailability(@PathVariable Long id, @RequestBody boolean available) {
        menuService.toggleAvailability(id, available);
        return ResponseEntity.ok(ApiResponseDto.success("Availability updated"));
    }
}
