package com.restaurant.order;

import com.restaurant.order.entity.ServiceableArea;
import com.restaurant.order.repository.ServiceableAreaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    // Seed serviceable areas (Hyderabad pincodes)
    @Bean
    CommandLineRunner seedAreas(ServiceableAreaRepository repository) {
        return args -> {
            if (repository.findByPincode("500032").isEmpty()) {
                repository.save(new ServiceableArea("500032", "Gachibowli", 35, 0.0));
            }
            if (repository.findByPincode("500081").isEmpty()) {
                repository.save(new ServiceableArea("500081", "Hitech City", 45, 30.0));
            }
            if (repository.findByPincode("500033").isEmpty()) {
                repository.save(new ServiceableArea("500033", "Jubilee Hills", 50, 40.0));
            }
            if (repository.findByPincode("500034").isEmpty()) {
                repository.save(new ServiceableArea("500034", "Banjara Hills", 55, 50.0));
            }
            // Add more as needed
        };
    }
}