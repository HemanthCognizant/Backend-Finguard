package com.finguard.controller;

import com.finguard.dto.ChartDataDTO;
import com.finguard.repository.AlertRepository;
import com.finguard.repository.CustomerOnboardingRepository;
import com.finguard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AnalyticsController {
    private final TransactionRepository txRepo;
    private final AlertRepository alertRepo;
    private final CustomerOnboardingRepository onboardingRepo;

    @GetMapping("/risk-distribution")
    public List<ChartDataDTO> getRisk() { return txRepo.getRiskDistribution(); }

    @GetMapping("/status-breakdown")
    public List<ChartDataDTO> getStatus() { return txRepo.getStatusBreakdown(); }

    @GetMapping("/alert-severity")
    public List<ChartDataDTO> getAlerts() { return alertRepo.getAlertSeverityCount(); }

    @GetMapping("/channel-volume")
    public List<ChartDataDTO> getChannels() { return txRepo.getVolumeByChannel(); }

    @GetMapping("/onboarding-funnel")
    public List<ChartDataDTO> getFunnel() { return onboardingRepo.getOnboardingStatus(); }

}
