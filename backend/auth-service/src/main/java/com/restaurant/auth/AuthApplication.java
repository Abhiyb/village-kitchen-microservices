package com.restaurant.auth;

import com.restaurant.auth.entity.User;
import com.restaurant.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByEmail("abhishekh.yb.dev@gmail.com").isEmpty()) {
                User admin = new User();
                admin.setName("Restaurant Admin");
                admin.setEmail("abhishekh.yb.dev@gmail.com");
                admin.setRole("ADMIN");
                userRepository.save(admin);
            }
        };
    }
}