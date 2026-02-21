package com.finguard.service;

import com.finguard.entity.CustomerOnboarding;
import com.finguard.entity.User;
import com.finguard.repository.CustomerOnboardingRepository;
import com.finguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerOnboardingService {

    private final UserRepository userRepository;

    private final CustomerOnboardingRepository repository;

    public CustomerOnboarding create(CustomerOnboarding customer) {
        customer.setApplicationId(
                "KYC" + UUID.randomUUID()
                        .toString().substring(0,5).toUpperCase()
        );
        customer.setStatus("PENDING");
        customer.setCreatedAt(LocalDateTime.now());
        return repository.save(customer);
    }

    public List<CustomerOnboarding> getAll() {
        return repository.findAll();
    }

    public CustomerOnboarding updateStatus(String applicationId, String status) {
        CustomerOnboarding customer = repository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));
        customer.setStatus(status);
        return repository.save(customer);
    }

    public CustomerOnboarding findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fix: Add .orElse(null) to resolve the Optional type mismatch
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
