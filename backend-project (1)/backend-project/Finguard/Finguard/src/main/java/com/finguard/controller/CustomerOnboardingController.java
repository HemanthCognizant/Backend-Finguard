package com.finguard.controller;

import com.finguard.entity.CustomerOnboarding;
import com.finguard.service.CustomerOnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CustomerOnboardingController {

    private final CustomerOnboardingService service;

    @PostMapping
    public ResponseEntity<CustomerOnboarding> create(@RequestBody CustomerOnboarding customer) {
        // The @PrePersist in the Entity handles date and ID generation now
        CustomerOnboarding saved = service.create(customer);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<CustomerOnboarding> getAll() {
        return service.getAll();
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String applicationId, // Must be String
            @RequestParam String status) {
        service.updateStatus(applicationId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<CustomerOnboarding> getProfile(@PathVariable Long userId) {
        // Logic to find onboarding details linked to this User ID
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

        // Helper to save files
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
