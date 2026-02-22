package com.finguard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor  // Summary: Fixes "Expected no arguments" error for Hibernate
@AllArgsConstructor // Summary: Fixes "Expected no arguments but found 5" error in Services
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String user;
    private String action;
    private String module;
    private String details;
    private String ipAddress;

    /**
     * Summary: Constructor for manual creation in Services.
     * Sets the timestamp automatically at the moment of creation.
     */
    public AuditLog(String user, String action, String module, String details, String ipAddress) {
        this.user = user;
        this.action = action;
        this.module = module;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        // Summary: Ensure timestamp is set if not already provided by the constructor
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
        // Note: Hardcoded IP removed from here so it can be passed
        // dynamically from HttpServletRequest in your Services.
    }
}