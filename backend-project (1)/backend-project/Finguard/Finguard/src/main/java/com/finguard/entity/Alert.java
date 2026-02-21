package com.finguard.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Alert {
    @Id
    private String id; // Format: ALT-001

    private String type; // "FAILED_LOGIN" or "HIGH_RISK_TX"
    private String customer;
    private String severity; // "HIGH", "MEDIUM", "LOW"
    private LocalDateTime timestamp;
    private String status; // "open", "in-progress", "closed"

    // Custom ID Generation Logic
    @PrePersist
    public void generateId() {
        // Simple logic: uses timestamp/random to ensure uniqueness in ALT-XXX format
        // In a production app, you'd query the count from DB to get sequential 001, 002...
        this.id = "ALT-" + String.format("%03d", (int)(Math.random() * 1000));
        this.timestamp = LocalDateTime.now();
        if (this.status == null) this.status = "open";
    }
}