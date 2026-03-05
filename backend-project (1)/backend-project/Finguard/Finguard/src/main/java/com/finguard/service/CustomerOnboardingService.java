package com.finguard.service;

import com.finguard.entity.AuditLog;
import com.finguard.entity.CustomerOnboarding;
import com.finguard.entity.Role;
import com.finguard.entity.User;
import com.finguard.repository.AuditRepository;
import com.finguard.repository.CustomerOnboardingRepository;
import com.finguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOnboardingService {

    private final UserRepository userRepository;
    private final CustomerOnboardingRepository customerOnboardingRepository;
    private final AuditRepository auditRepository;

    @Value("${file.upload-dir:./uploads/documents}")
    private String uploadDir;

    // () -> to create new onboarding record
    public CustomerOnboarding create(CustomerOnboarding customer,
                                     MultipartFile aadhaarFront, // MutlipartFile -> interface that provides essential methods for handling the uploaded data.
                                     MultipartFile aadhaarBack,
                                     MultipartFile panCard,
                                     MultipartFile photo,
                                     HttpServletRequest request) throws IOException {

        // 1. Save physical files and set names in the entity
        customer.setAadhaarFront(savePhysicalFile(aadhaarFront));
        customer.setAadhaarBack(savePhysicalFile(aadhaarBack));
        customer.setPanCard(savePhysicalFile(panCard));
        customer.setPhoto(savePhysicalFile(photo));

        // 2. Set metadata
        customer.setApplicationId("KYC" + UUID.randomUUID().toString().substring(0,5).toUpperCase());
        customer.setStatus("PENDING");
        customer.setCreatedAt(LocalDateTime.now());

        CustomerOnboarding saved = customerOnboardingRepository.save(customer);

        // 3. Audit Logging
        String ip = request.getHeader("X-Forwarded-For"); // XFF is used to retrieve the ip from header
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr(); // we may get proxy server ip, not the real ip
        }
        auditRepository.save(new AuditLog(
                SecurityContextHolder.getContext().getAuthentication().getName(),
                Role.BANKER.name(),
                "Customer Onboarding",
                "KYC",
                "Onboarded: " + saved.getFullName(),
                ip));

        return saved;
    }

    private String savePhysicalFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    public List<CustomerOnboarding> getAll() {
        return customerOnboardingRepository.findAll();
    }

    public CustomerOnboarding updateStatus(String applicationId, String status, HttpServletRequest request) {
        CustomerOnboarding customer = customerOnboardingRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));
        customer.setStatus(status);
        CustomerOnboarding updated = customerOnboardingRepository.save(customer);

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if ("APPROVED".equalsIgnoreCase(status)) {
            auditRepository.save(new AuditLog(
                    SecurityContextHolder.getContext().getAuthentication().getName(),
                    Role.ADMIN.name(),
                    "KYC Approved",
                    "KYC Verification",
                    "Approved KYC for: " + updated.getFullName(),
                    ip));
        }

        return updated;
    }

    public CustomerOnboarding findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerOnboarding profile = customerOnboardingRepository.findByEmail(user.getEmail()).orElse(null);

        if (profile == null) {
            CustomerOnboarding fallback = new CustomerOnboarding();
            fallback.setFullName(user.getName());
            fallback.setApplicationId("PENDING");
            fallback.setStatus("NOT_STARTED");
            return fallback;
        }
        return profile;
    }
}