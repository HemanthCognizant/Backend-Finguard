package com.finguard.controller;
import com.finguard.dto.TransactionRequest;
import com.finguard.entity.CustomerOnboarding;
import com.finguard.entity.Transaction;
import com.finguard.service.CustomerOnboardingService;
import com.finguard.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin
public class TransactionController {
    private final TransactionService transactionService;
    private final CustomerOnboardingService service;
    @PostMapping
    public ResponseEntity<?> send(@RequestBody TransactionRequest request) {
        Transaction tx = transactionService.sendTransaction(
                request.getSenderId(),
                request.getRecipientId(),
                request.getAmount(),
                request.getChannel(),
                request.getPassword()
        );
        return ResponseEntity.ok(tx);
    }
    @GetMapping("/{userId}")
    public ResponseEntity<?> history(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getHistory(userId));
    }
    @GetMapping("/balance/{userId}")
    public ResponseEntity<?> balance(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getBalance(userId));
    }
    // Fetch all transactions for the Banker/Admin Monitoring view
    @GetMapping("/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    // Update status (Approve/Reject)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        transactionService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }
}