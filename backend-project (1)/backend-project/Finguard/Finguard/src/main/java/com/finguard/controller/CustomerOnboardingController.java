package com.finguard.controller;

import com.finguard.entity.CustomerOnboarding;
import com.finguard.service.CustomerOnboardingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CustomerOnboardingController {

    private final CustomerOnboardingService service;

    // --- OTP ENDPOINTS ---

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> response = new HashMap<>();

        try {
            // Match this to the method name in your Service (sendOtp)
            service.sendOtp(email);
            response.put("message", "OTP sent successfully to " + email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending OTP: ", e);
            response.put("error", "Failed to send OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        boolean isValid = service.verifyOtp(email, otp);

        Map<String, Object> response = new HashMap<>();
        if (isValid) {
            response.put("success", true);
            response.put("message", "OTP Verified");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid or expired OTP");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // --- EXISTING ONBOARDING ENDPOINTS ---

    @PostMapping
    public ResponseEntity<CustomerOnboarding> create(@RequestBody CustomerOnboarding customer) {
        CustomerOnboarding saved = service.create(customer);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<CustomerOnboarding> getAll() {
        return service.getAll();
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String applicationId,
            @RequestParam String status) {
        service.updateStatus(applicationId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<CustomerOnboarding> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(service.findByUserId(userId));
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, String>> uploadFiles(
            @RequestParam("aadhaarFront") MultipartFile aadhaarFront,
            @RequestParam("aadhaarBack") MultipartFile aadhaarBack,
            @RequestParam("panCard") MultipartFile panCard,
            @RequestParam("photo") MultipartFile photo) throws Exception {

        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        Map<String, String> fileNames = new HashMap<>();
        fileNames.put("aadhaarFront", saveFile(aadhaarFront, uploadDir));
        fileNames.put("aadhaarBack", saveFile(aadhaarBack, uploadDir));
        fileNames.put("panCard", saveFile(panCard, uploadDir));
        fileNames.put("photo", saveFile(photo, uploadDir));

        return ResponseEntity.ok(fileNames);
    }

    private String saveFile(MultipartFile file, String dir) throws Exception {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        file.transferTo(new File(dir + fileName));
        return fileName;
    }
}