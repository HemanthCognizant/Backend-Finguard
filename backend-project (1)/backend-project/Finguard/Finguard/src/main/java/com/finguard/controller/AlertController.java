package com.finguard.controller;

import com.finguard.entity.Alert;
import com.finguard.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // Adjust based on your frontend port
public class AlertController {

    private final AlertRepository alertRepo;

    // Fetch all alerts for the table
    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertRepo.findAll());
    }

    // Fetch stats for the summary cards (Open, In-Progress, Closed)
    @GetMapping("/stats")
    public Map<String, Long> getAlertStats() {
        List<Alert> allAlerts = alertRepo.findAll();
        Map<String, Long> stats = new HashMap<>();

        stats.put("open", allAlerts.stream().filter(a -> "open".equalsIgnoreCase(a.getStatus())).count());
        stats.put("inProgress", allAlerts.stream().filter(a -> "in-progress".equalsIgnoreCase(a.getStatus())).count());
        stats.put("closed", allAlerts.stream().filter(a -> "closed".equalsIgnoreCase(a.getStatus())).count());

        return stats;
    }

    // Update status (e.g., when a banker clicks "Acknowledge")
    @PutMapping("/{id}/status")
    public Alert updateStatus(@PathVariable String id, @RequestParam String status) {
        Alert alert = alertRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setStatus(status.toLowerCase());
        return alertRepo.save(alert);
    }
}