package com.finguard.service;

import com.finguard.entity.CustomerOnboarding;
import com.finguard.entity.Transaction;
import com.finguard.entity.User;
import com.finguard.repository.CustomerOnboardingRepository;
import com.finguard.repository.TransactionRepository;
import com.finguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.finguard.entity.Role;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerOnboardingRepository onboardingRepository;
    private final PasswordEncoder passwordEncoder;

@Transactional
public Transaction sendTransaction(Long senderId,
                                   Long recipientId,
                                   Double amount,
                                   String channel,
                                   String password) {

    // 1. Fetch User entities and verify they exist
    User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
    User recipient = userRepository.findById(recipientId)
            .orElseThrow(() -> new RuntimeException("Recipient not found"));

    // 2. Role Validation: Ensure both are CUSTOMERS
    if (sender.getRole() != Role.CUSTOMER) {
        throw new RuntimeException("Only customers can initiate transactions");
    }
    // Note: If you allow sending to non-customers, remove the next check
    if (recipient.getRole() != Role.CUSTOMER) {
        throw new RuntimeException("Recipient must be a valid bank customer");
    }

    // 3. Security Check: Verify banking password
    if (!passwordEncoder.matches(password, sender.getPassword())) {
        throw new RuntimeException("Invalid banking password. Please try again.");
    }

    // 4. Validation: Logical checks
    if (senderId.equals(recipientId)) {
        throw new RuntimeException("Cannot send money to your own account");
    }
    if (amount <= 0) {
        throw new RuntimeException("Transaction amount must be greater than zero");
    }

    // 5. Map to Onboarding Table: Fetch records using Email as the bridge
    CustomerOnboarding senderKyc = onboardingRepository.findByEmail(sender.getEmail())
            .orElseThrow(() -> new RuntimeException("Sender account is not fully onboarded/KYC verified"));
    CustomerOnboarding recipientKyc = onboardingRepository.findByEmail(recipient.getEmail())
            .orElseThrow(() -> new RuntimeException("Recipient account is not fully onboarded/active"));

    // 6. Balance Check (using the Onboarding table balance)
    if (senderKyc.getBalance() < amount) {
        throw new RuntimeException("Insufficient balance in your onboarded account");
    }

    // 7. Risk Analysis
    String risk = (amount > 100000) ? "HIGH" : (amount > 50000) ? "MEDIUM" : "LOW";

    // 8. Status Logic: HIGH risk requires Banker approval (PENDING)
    String status = risk.equals("HIGH") ? "PENDING" : "SUCCESS";

    // 9. Create Transaction Record
    Transaction tx = new Transaction();
    tx.setSender(sender);
    tx.setRecipient(recipient);
    tx.setAmount(amount);
    tx.setChannel(channel);
    tx.setRiskLevel(risk);
    tx.setStatus(status);

    // 10. Atomic Balance Update: Only if the status is SUCCESS
    if (status.equals("SUCCESS")) {
        senderKyc.setBalance(senderKyc.getBalance() - amount);
        recipientKyc.setBalance(recipientKyc.getBalance() + amount);

        // Save the updated balances to the customer_onboarding table
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
    public void updateStatus(Long id, String status) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Only process balance if moving from PENDING to COMPLETED/SUCCESS
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
        } else {
            tx.setStatus(status.toUpperCase());
        }

        transactionRepository.save(tx);
    }
}
