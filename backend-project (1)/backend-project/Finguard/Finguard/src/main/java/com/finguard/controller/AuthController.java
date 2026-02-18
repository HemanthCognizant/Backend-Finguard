package com.finguard.controller;
import com.finguard.config.JwtUtil;
import com.finguard.dto.*;
import com.finguard.entity.User;
import com.finguard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService service;
    private final JwtUtil jwtUtil;
    public AuthController(UserService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .build();
        service.register(user); // ✅ correct

        return ResponseEntity.ok(
                Map.of("message", "Signup successful")
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        User user = service.authenticate(req.getEmail(), req.getPassword());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(),user.getId());
        return new LoginResponse(token);

    }

}
