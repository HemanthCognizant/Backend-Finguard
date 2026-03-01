package com.finguard.controller;
import com.finguard.dto.ChartData;
import com.finguard.repository.AlertRepository;
import com.finguard.repository.CustomerOnboardingRepository;
import com.finguard.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AnalyticsController {
    private final TransactionRepository txRepo;
    private final AlertRepository alertRepo;
    private final CustomerOnboardingRepository onboardingRepo;

    @GetMapping("/risk-distribution")
    public List<ChartData> getRisk() { return txRepo.getRiskDistribution(); }

    @GetMapping("/status-breakdown")
    public List<ChartData> getStatus() { return txRepo.getStatusBreakdown(); }

    @GetMapping("/alert-severity")
    public List<ChartData> getAlerts() { return alertRepo.getAlertSeverityCount(); }

    @GetMapping("/channel-volume")
    public List<ChartData> getChannels() { return txRepo.getVolumeByChannel(); }

    @GetMapping("/onboarding-funnel")
    public List<ChartData> getFunnel() { return onboardingRepo.getOnboardingStatus(); }

}
