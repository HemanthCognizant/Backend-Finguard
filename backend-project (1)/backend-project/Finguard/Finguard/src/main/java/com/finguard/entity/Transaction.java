package com.finguard.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity

@Table(name = "transactions")

@Data

public class Transaction {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

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

}
