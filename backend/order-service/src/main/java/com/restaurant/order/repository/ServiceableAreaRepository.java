package com.restaurant.order.repository;

import com.restaurant.order.entity.ServiceableArea;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceableAreaRepository extends JpaRepository<ServiceableArea, Long> {
    Optional<ServiceableArea> findByPincode(String pincode);
}