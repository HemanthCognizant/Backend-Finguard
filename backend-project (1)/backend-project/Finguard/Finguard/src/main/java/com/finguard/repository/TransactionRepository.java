package com.finguard.repository;

import com.finguard.dto.ChartDataDTO;
import com.finguard.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findBySenderId(Long senderId);

    // 1. Risk Level Distribution & 2. Status Breakdown & 4. Channel Volume
    @Query("SELECT new com.finguard.dto.ChartDataDTO(t.riskLevel, COUNT(t)) FROM Transaction t GROUP BY t.riskLevel")
    List<ChartDataDTO> getRiskDistribution();

    @Query("SELECT new com.finguard.dto.ChartDataDTO(t.status, COUNT(t)) FROM Transaction t GROUP BY t.status")
    List<ChartDataDTO> getStatusBreakdown();

    @Query("SELECT new com.finguard.dto.ChartDataDTO(t.channel, COUNT(t)) FROM Transaction t GROUP BY t.channel")
    List<ChartDataDTO> getVolumeByChannel();
}