package com.restaurant.order.repository;

import com.restaurant.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    // For admin revenue
    @Query("SELECT SUM(o.finalAmount) FROM Order o " +
            "WHERE FUNCTION('DATE', o.createdAt) = CURRENT_DATE " +
            "AND o.paymentStatus = 'SUCCESS'")
    Double getDailyRevenue();
}