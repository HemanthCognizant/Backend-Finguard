package com.finguard.service;

import com.finguard.entity.AuditLog;
import com.finguard.entity.CustomerOnboarding;
import com.finguard.entity.User;
import com.finguard.repository.AuditRepository;
import com.finguard.repository.CustomerOnboardingRepository;

import com.finguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOnboardingService {

    private final UserRepository userRepository;
    private final CustomerOnboardingRepository repository;

    private final JavaMailSender mailSender;
    private final AuditRepository auditRepo;
    private final HttpServletRequest request;


    public CustomerOnboarding create(CustomerOnboarding customer, HttpServletRequest request) {
        customer.setApplicationId(
                "KYC" + UUID.randomUUID().toString().substring(0,5).toUpperCase()
        );
        customer.setStatus("PENDING");
        customer.setCreatedAt(LocalDateTime.now());

        CustomerOnboarding saved = repository.save(customer);

        // Summary: Real IP extraction and audit log for KYC creation
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        auditRepo.save(new AuditLog("Banker Name", "BANKER", "Customer Onboarding", "KYC", "Onboarded: " + saved.getFullName(), ip));

        return saved;
    }

    public List<CustomerOnboarding> getAll() {
        return repository.findAll();
    }

    public CustomerOnboarding updateStatus(String applicationId, String status) {
        CustomerOnboarding customer = repository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));
        customer.setStatus(status);
        CustomerOnboarding updated = repository.save(customer);

        // Summary: Only generate log if status is APPROVED; captures real IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if ("APPROVED".equalsIgnoreCase(status)) {
            auditRepo.save(new AuditLog("Admin", "ADMIN", "KYC Approved", "KYC Verification", "Approved KYC for: " + updated.getFullName(), ip));
        }

        return updated;
    }

    public CustomerOnboarding findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerOnboarding profile = repository.findByEmail(user.getEmail()).orElse(null);

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