package com.finguard.controller;

import com.finguard.entity.CustomerOnboarding;
import com.finguard.service.CustomerOnboardingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CustomerOnboardingController {

    private final CustomerOnboardingService service;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<CustomerOnboarding> create(
            @RequestPart("customer") CustomerOnboarding customer,
            @RequestPart("aadhaarFront") MultipartFile aadhaarFront,
            @RequestPart("aadhaarBack") MultipartFile aadhaarBack,
            @RequestPart("panCard") MultipartFile panCard,
            @RequestPart("photo") MultipartFile photo,
            HttpServletRequest request) throws Exception {

        CustomerOnboarding saved = service.create(customer, aadhaarFront, aadhaarBack, panCard, photo, request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<CustomerOnboarding> getAll() {
        return service.getAll();
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String applicationId,
            @RequestParam String status,
            HttpServletRequest request) {
        service.updateStatus(applicationId, status, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<CustomerOnboarding> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }
}