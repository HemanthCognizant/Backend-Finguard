package com.finguard.controller;

import com.finguard.entity.CustomerOnboarding;
import com.finguard.service.CustomerOnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController

@RequestMapping("/api/onboarding")

@RequiredArgsConstructor

@CrossOrigin

public class CustomerOnboardingController {

    private final CustomerOnboardingService service;

    @PostMapping

    public CustomerOnboarding create(

            @RequestBody CustomerOnboarding customer) {

        return service.create(customer);

    }

    @GetMapping

    public List<CustomerOnboarding> getAll() {

        return service.getAll();

    }

    @PutMapping("/{id}/status")

    public CustomerOnboarding updateStatus(

            @PathVariable Long id,

            @RequestParam String status) {

        return service.updateStatus(id, status);

    }
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file)
            throws Exception {
        String uploadDir = "uploads/";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        String filePath = uploadDir + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        return filePath;
    }

}
