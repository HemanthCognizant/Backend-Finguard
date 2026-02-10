package com.finguard.controller;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping
public class UserController {
    @GetMapping("/admin/dashboard")
    public Map<String, String> adminDashboard() {
        return Map.of(
                "message", "Welcome Admin",
                "access", "ADMIN dashboard data loaded"
        );
    }
    @GetMapping("/banker/dashboard")
    public Map<String, String> bankerDashboard() {
        return Map.of(
                "message", "Welcome Banker",
                "access", "BANKER dashboard data loaded"
        );
    }
    @GetMapping("/customer/dashboard")
    public Map<String, String> customerDashboard() {
        return Map.of(
                "message", "Welcome Customer",
                "access", "CUSTOMER dashboard data loaded"
        );
    }
    @GetMapping("/user/profile")
    public Map<String, Object> profile(Authentication auth) {
        return Map.of(
                "username", auth.getName(),
                "roles", auth.getAuthorities()
        );
    }
}
