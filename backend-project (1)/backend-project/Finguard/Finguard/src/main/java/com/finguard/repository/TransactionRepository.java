package com.finguard.repository;

import com.finguard.dto.ChartData;
import com.finguard.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findBySenderId(Long senderId);


    @Query("SELECT new com.finguard.dto.ChartData(t.riskLevel, COUNT(t)) FROM Transaction t GROUP BY t.riskLevel")
    List<ChartData> getRiskDistribution();

    @Query("SELECT new com.finguard.dto.ChartData(t.status, COUNT(t)) FROM Transaction t GROUP BY t.status")
    List<ChartData> getStatusBreakdown();

    @Query("SELECT new com.finguard.dto.ChartData(t.channel, COUNT(t)) FROM Transaction t GROUP BY t.channel")
    List<ChartData> getVolumeByChannel();
}