package com.restaurant.menu.repository;

import com.restaurant.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    @Query("SELECT m FROM MenuItem m WHERE UPPER(m.category.type) = UPPER(:type)")
    List<MenuItem> findByCategoryType(@Param("type") String type); // veg and non veg filter
    List<MenuItem> findByAvailable(boolean available);
}