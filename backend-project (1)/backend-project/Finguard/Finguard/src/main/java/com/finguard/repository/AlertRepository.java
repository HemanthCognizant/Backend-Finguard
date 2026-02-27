package com.finguard.repository;

import com.finguard.dto.ChartDataDTO;
import com.finguard.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    @Query("SELECT new com.finguard.dto.ChartDataDTO(a.severity, COUNT(a)) FROM Alert a GROUP BY a.severity")
    List<ChartDataDTO> getAlertSeverityCount();
    long countBySeverity(String severity);
}
