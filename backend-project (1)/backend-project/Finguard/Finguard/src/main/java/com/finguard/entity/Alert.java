package com.finguard.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Alert {
    @Id
    private String id;
    private String type;
    private String customer;
    private String severity;
    private LocalDateTime timestamp;
    private String status;
    @PrePersist
    public void generateId() {
        this.id = "ALT-" + String.format("%03d", (int)(Math.random() * 1000));
        this.timestamp = LocalDateTime.now();
        if (this.status == null) this.status = "open";
    }
}