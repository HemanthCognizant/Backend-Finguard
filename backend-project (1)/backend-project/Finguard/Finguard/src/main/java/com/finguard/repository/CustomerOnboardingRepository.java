package com.finguard.repository;

import com.finguard.dto.ChartDataDTO;
import com.finguard.entity.CustomerOnboarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerOnboardingRepository extends JpaRepository<CustomerOnboarding, String> {
    Optional<CustomerOnboarding> findByEmail(String email);


    @Query("SELECT new com.finguard.dto.ChartDataDTO(c.status, COUNT(c)) FROM CustomerOnboarding c GROUP BY c.status")
    List<ChartDataDTO> getOnboardingStatus();
}