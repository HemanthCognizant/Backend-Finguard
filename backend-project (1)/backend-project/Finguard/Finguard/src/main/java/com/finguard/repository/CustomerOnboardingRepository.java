package com.finguard.repository;

import com.finguard.entity.CustomerOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerOnboardingRepository extends JpaRepository<CustomerOnboarding, String> {
    Optional<CustomerOnboarding> findByEmail(String email);
}