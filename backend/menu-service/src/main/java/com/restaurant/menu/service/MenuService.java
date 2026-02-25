package com.restaurant.menu.service;

import com.restaurant.menu.dto.CategoryDto;
import com.restaurant.menu.dto.MenuItemDto;
import com.restaurant.menu.entity.Category;
import com.restaurant.menu.entity.MenuItem;
import com.restaurant.menu.exception.ResourceNotFoundException;
import com.restaurant.menu.repository.CategoryRepository;
import com.restaurant.menu.repository.MenuItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    public MenuService(CategoryRepository categoryRepository, MenuItemRepository menuItemRepository) {
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
    }

    // Category methods
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setActive(dto.isActive());
        Category saved = categoryRepository.save(category);
        dto.setId(saved.getId());
        return dto;
    }

    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setName(dto.getName());
        category.setType(dto.getType());
        category.setActive(dto.isActive());
        Category updated = categoryRepository.save(category);
        dto.setId(updated.getId());
        return dto;
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return toCategoryDto(category);
    }

    private CategoryDto toCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setType(category.getType());
        dto.setActive(category.isActive());
        return dto;
    }

    // Menu Item methods
    public MenuItemDto createMenuItem(MenuItemDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
        MenuItem item = new MenuItem();
        item.setCategory(category);
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setAvailable(dto.isAvailable());
        item.setImageUrl(dto.getImageUrl());
        item.setPreparationTimeMinutes(dto.getPreparationTimeMinutes());
        MenuItem saved = menuItemRepository.save(item);
        dto.setId(saved.getId());
        return dto;
    }

    public MenuItemDto updateMenuItem(Long id, MenuItemDto dto) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
            item.setCategory(category);
        }
        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getPrice() > 0) item.setPrice(dto.getPrice());
        item.setAvailable(dto.isAvailable());
        if (dto.getImageUrl() != null) item.setImageUrl(dto.getImageUrl());
        if (dto.getPreparationTimeMinutes() > 0) item.setPreparationTimeMinutes(dto.getPreparationTimeMinutes());
        MenuItem updated = menuItemRepository.save(item);
        dto.setId(updated.getId());
        return dto;
    }

    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu item not found with id: " + id);
        }
        menuItemRepository.deleteById(id);
    }

    public List<MenuItemDto> getAllMenuItems(String type) {
        List<MenuItem> items;
        if (type != null) {
            items = menuItemRepository.findByCategoryType(type.toUpperCase());
        } else {
            items = menuItemRepository.findAll();
        }
        return items.stream()
                .map(this::toMenuItemDto)
                .collect(Collectors.toList());
    }

    public MenuItemDto getMenuItemById(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        return toMenuItemDto(item);
    }

    public void toggleAvailability(Long id, boolean available) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));
        item.setAvailable(available);
        menuItemRepository.save(item);
    }

    private MenuItemDto toMenuItemDto(MenuItem item) {
        MenuItemDto dto = new MenuItemDto();
        dto.setId(item.getId());
        dto.setCategoryId(item.getCategory().getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setPrice(item.getPrice());
        dto.setAvailable(item.isAvailable());
        dto.setImageUrl(item.getImageUrl());
        dto.setPreparationTimeMinutes(item.getPreparationTimeMinutes());
        return dto;
    }
}