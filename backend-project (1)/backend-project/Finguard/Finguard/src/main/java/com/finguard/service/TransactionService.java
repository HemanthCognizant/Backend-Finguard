package com.finguard.service;

import com.finguard.dto.TransactionSummary;
import com.finguard.entity.*;
import com.finguard.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerOnboardingRepository onboardingRepository;
    private final PasswordEncoder passwordEncoder;
    private final AlertRepository alertRepo;
    private final AuditRepository auditRepo;
    private final HttpServletRequest request;

    @Transactional
    public Transaction sendTransaction(Long senderId,
                                       String recipientAppId,
                                       Double amount,
                                       String channel,
                                       String password) {

        // 1. Fetch Sender User and their KYC details
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        CustomerOnboarding senderKyc = onboardingRepository.findByEmail(sender.getEmail())
                .orElseThrow(() -> new RuntimeException("Sender KYC record not found"));

        // 2. Fetch Recipient by Application ID (from customer_onboarding table)
        CustomerOnboarding recipientKyc = onboardingRepository.findById(recipientAppId)
                .orElseThrow(() -> new RuntimeException("Recipient Application ID not found: " + recipientAppId));

        // 3. Bridge Recipient KYC to User Table via Email to maintain Transaction Relationship
        User recipient = userRepository.findByEmail(recipientKyc.getEmail())
                .orElseThrow(() -> new RuntimeException("Recipient user account is not active or registered"));

        // 4. Role Validation
        if (sender.getRole() != Role.CUSTOMER) {
            throw new RuntimeException("Only customers can initiate transactions");
        }
        if (!"APPROVED".equalsIgnoreCase(senderKyc.getStatus())) {
            throw new RuntimeException("Transaction failed: Your KYC status is " + senderKyc.getStatus() + ". Only APPROVED customers can send money.");
        }

        if (!"APPROVED".equalsIgnoreCase(recipientKyc.getStatus())) {
            throw new RuntimeException("Transaction failed: The recipient's KYC status is " + recipientKyc.getStatus() + ". Money can only be sent to APPROVED accounts.");
        }

        // 5. Security Check: Verify banking password
        if (!passwordEncoder.matches(password, sender.getPassword())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Invalid banking password. Please try again."
            );
        }

        // 6. Logical Validations
        if (senderId.equals(recipient.getId())) {
            throw new RuntimeException("Cannot send money to your own account");
        }
        if (amount <= 0) {
            throw new RuntimeException("Transaction amount must be greater than zero");
        }

        // 7. Balance Check (using the Onboarding table balance)
        if (senderKyc.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance in your account");
        }

        // 8. Risk Analysis
        String risk = (amount > 100000) ? "HIGH" : (amount > 50000) ? "MEDIUM" : "LOW";

        if ("HIGH".equals(risk)) {
            Alert alert = new Alert();
            alert.setType("Unusual Transaction Pattern");
            alert.setCustomer(sender.getName());
            alert.setSeverity("HIGH");
            alertRepo.save(alert);
        }

        // 9. Status Logic: HIGH risk requires Banker approval (PENDING)
        String status = risk.equals("HIGH") ? "PENDING" : "SUCCESS";

        // 10. Create Transaction Record
        Transaction tx = new Transaction();
        tx.setSender(sender);
        tx.setRecipient(recipient);
        tx.setRecipientAppId(recipientAppId);
        tx.setAmount(amount);
        tx.setChannel(channel);
        tx.setRiskLevel(risk);
        tx.setStatus(status);

        // 11.Balance Update-Only if the status is SUCCESS
        if (status.equals("SUCCESS")) {
            senderKyc.setBalance(senderKyc.getBalance() - amount);
            recipientKyc.setBalance(recipientKyc.getBalance() + amount);
            onboardingRepository.save(senderKyc);
            onboardingRepository.save(recipientKyc);
        }

        return transactionRepository.save(tx);
    }

    public List<Transaction> getHistory(Long userId) {
        return transactionRepository.findBySenderId(userId);
    }

    public Double getBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return onboardingRepository.findByEmail(user.getEmail())
                .map(CustomerOnboarding::getBalance)
                .orElseThrow(() -> new RuntimeException("KYC record not found for this user"));
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Transactional
    public void updateStatus(String id, String status) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (status.equalsIgnoreCase("COMPLETED") && !tx.getStatus().equals("SUCCESS")) {
            CustomerOnboarding senderKyc = onboardingRepository.findByEmail(tx.getSender().getEmail())
                    .orElseThrow(() -> new RuntimeException("Sender KYC missing"));
            CustomerOnboarding recipientKyc = onboardingRepository.findByEmail(tx.getRecipient().getEmail())
                    .orElseThrow(() -> new RuntimeException("Recipient KYC missing"));

            senderKyc.setBalance(senderKyc.getBalance() - tx.getAmount());
            recipientKyc.setBalance(recipientKyc.getBalance() + tx.getAmount());

            onboardingRepository.save(senderKyc);
            onboardingRepository.save(recipientKyc);
            tx.setStatus("SUCCESS");

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
            auditRepo.save(new AuditLog("Admin", "ADMIN", "Approved Transaction", "Risk Management",
                    "Approved High Risk TX: " + tx.getId(), ip));
        } else {
            tx.setStatus(status.toUpperCase());
        }

        transactionRepository.save(tx);
    }

    public TransactionSummary getTransactionSummary() {
        List<Transaction> all = transactionRepository.findAll();

        long totalToday = all.size();

        long pending = all.stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()))
                .count();

        long blocked = all.stream()
                .filter(t -> "BLOCKED".equalsIgnoreCase(t.getStatus()))
                .count();

        double sum = all.stream()
                .mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                .sum();

        return new TransactionSummary(totalToday, pending, blocked, sum);
    }
}