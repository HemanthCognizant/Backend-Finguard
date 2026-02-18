package com.finguard.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {

    @Id
    @Column(name = "tx_id") // Renamed column
    private String id; // Changed type to String for "TX-XXXX" format

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    private Double amount;

    private String channel; // UPI / ONLINE_BANKING

    private String riskLevel; // LOW / MEDIUM / HIGH

    private String status; // SUCCESS / PENDING

    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void generateId() {
        int randomNum = (int) (Math.random() * 9000) + 1000; // Generates 4 digits (1000-9999)
        this.id = "TX-" + randomNum;
    }

}
