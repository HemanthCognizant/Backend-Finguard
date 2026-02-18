package com.finguard.controller;
import com.finguard.dto.TransactionRequest;
import com.finguard.entity.Transaction;
import com.finguard.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin
public class TransactionController {
    private final TransactionService transactionService;
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
    public ResponseEntity<?> balance(@PathVariable Long userId) { // Changed from @RequestParam email
        return ResponseEntity.ok(transactionService.getBalance(userId)); // Changed to getBalance
    }
}