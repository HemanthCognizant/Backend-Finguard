package com.finguard.controller;

import com.finguard.entity.AuditLog;
import com.finguard.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // Summary: Allows Angular to access this API
public class AuditLogController {

    private final AuditRepository auditRepository;

    // Summary: Fetches all logs from the database, sorted by newest first
    @GetMapping
    public List<AuditLog> getAllLogs() {
        return auditRepository.findAllByOrderByTimestampDesc();
    }
}