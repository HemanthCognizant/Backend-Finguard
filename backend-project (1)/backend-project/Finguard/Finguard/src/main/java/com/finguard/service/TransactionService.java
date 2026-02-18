package com.finguard.service;

import com.finguard.entity.Transaction;
import com.finguard.entity.User;
import com.finguard.repository.TransactionRepository;
import com.finguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    public Transaction sendTransaction(Long senderId,
                                       Long recipientId,
                                       Double amount,
                                       String channel,
                                       String password) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        if (senderId.equals(recipientId)) {
            throw new RuntimeException("Cannot send to yourself");
        }
        if (amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }
        if (sender.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }
        // ✅ Risk Logic
        String risk;
        if (amount > 100000) {
            risk = "HIGH";
        } else if (amount > 50000) {
            risk = "MEDIUM";
        } else {
            risk = "LOW";
        }
        String status = risk.equals("HIGH") ? "PENDING" : "SUCCESS";
        Transaction tx = new Transaction();
        tx.setSender(sender);
        tx.setRecipient(recipient);
        tx.setAmount(amount);
        tx.setChannel(channel);
        tx.setRiskLevel(risk);
        tx.setStatus(status);
        // Deduct balance only if SUCCESS
        if (status.equals("SUCCESS")) {
            sender.setBalance(sender.getBalance() - amount);
            recipient.setBalance(recipient.getBalance() + amount);
            userRepository.save(sender);
            userRepository.save(recipient);
        }
        return transactionRepository.save(tx);
    }
    public List<Transaction> getHistory(Long userId) {
        return transactionRepository.findBySenderId(userId);
    }
    public Double getBalance(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow()
                .getBalance();
    }
}
