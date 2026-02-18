package com.finguard.dto;
import lombok.Data;

@Data
public class TransactionRequest {
    private Long senderId;
    private Long recipientId;
    private Double amount;
    private String channel;
    private String password; // mock
}