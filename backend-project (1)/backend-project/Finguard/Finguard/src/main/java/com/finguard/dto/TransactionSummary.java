package com.finguard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionSummary {
    private long totalToday;
    private long pendingCount;
    private long blockedCount;
    private Double totalAmount;
}