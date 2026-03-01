package com.finguard.controller;

import com.finguard.entity.AuditLog;
import com.finguard.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuditLogController {

    private final AuditRepository auditRepository;
    @GetMapping
    public List<AuditLog> getAllLogs() {
        return auditRepository.findAllByOrderByTimestampDesc();
    }
}