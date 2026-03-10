package com.finguard.controller;

import com.finguard.entity.AuditLog;
import com.finguard.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuditLogController {

    private final AuditRepository auditRepository;
    @GetMapping
    public Page<AuditLog> getAllLogs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return auditRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
    }
}