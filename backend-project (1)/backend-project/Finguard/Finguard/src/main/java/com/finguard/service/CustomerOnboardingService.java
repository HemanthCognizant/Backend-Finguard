package com.finguard.service;

import com.finguard.entity.CustomerOnboarding;
import com.finguard.repository.CustomerOnboardingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerOnboardingService {

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
                .orElseThrow(()->new RuntimeException("Application not found: " + applicationId));
        customer.setStatus(status);
        return repository.save(customer);
    }

}
