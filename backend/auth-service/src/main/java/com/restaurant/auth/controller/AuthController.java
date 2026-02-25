package com.restaurant.auth.controller;

import com.restaurant.auth.dto.ApiResponseDto;
import com.restaurant.auth.dto.OtpRequestDto;
import com.restaurant.auth.dto.OtpVerifyRequestDto;
import com.restaurant.auth.dto.RegisterRequestDto;
import com.restaurant.auth.dto.TokenResponseDto;
import com.restaurant.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<?>> register(@Valid @RequestBody RegisterRequestDto dto) {
        var user = authService.register(dto.getName(), dto.getEmail());
        return ResponseEntity.ok(ApiResponseDto.success(user, "Registration successful"));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponseDto<?>> sendOtp(@Valid @RequestBody OtpRequestDto dto) {
        try {
            authService.sendOtp(dto.getEmail());
            return ResponseEntity.ok(ApiResponseDto.success(null, "OTP sent successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.error("User not registered. Please register first."));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponseDto<TokenResponseDto>> verifyOtp(@Valid @RequestBody OtpVerifyRequestDto dto) {
        try {
            String token = authService.verifyAndLogin(dto.getEmail(), dto.getOtp());
            TokenResponseDto response = new TokenResponseDto();
            response.setToken(token);
            return ResponseEntity.ok(ApiResponseDto.success(response, "Login successful"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponseDto.<TokenResponseDto>error(e.getMessage()));
        }
    }

    @GetMapping("/protected-test")
    public ResponseEntity<String> protectedTest(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok("JWT is valid! Hello, " + email + " - you are authenticated.");
    }
}