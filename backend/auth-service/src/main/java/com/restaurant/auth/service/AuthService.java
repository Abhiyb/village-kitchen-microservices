package com.restaurant.auth.service;

import com.restaurant.auth.config.JwtUtil;
import com.restaurant.auth.entity.User;
import com.restaurant.auth.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, OtpService otpService, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
    }

    public User register(String name, String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole("CUSTOMER");
        return userRepository.save(user);
    }

    public void sendOtp(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        otpService.generateAndSend(email);
    }

    public String verifyAndLogin(String email, String otp) {
        if (!otpService.verify(email, otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP");
        }
        User user = userRepository.findByEmail(email).orElseThrow();
        return jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
    }
}
