package com.finguard.repository;

import com.finguard.entity.CustomerOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOnboardingRepository
        extends JpaRepository<CustomerOnboarding, Long> {
}