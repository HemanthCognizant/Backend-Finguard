package com.finguard.controller;

import com.finguard.entity.CustomerOnboarding;
import com.finguard.service.CustomerOnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;

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

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String filePath = uploadDir + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        return file.getOriginalFilename(); // Return just the name for DB storage
    }
}
