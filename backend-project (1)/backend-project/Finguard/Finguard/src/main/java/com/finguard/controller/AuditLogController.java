package com.finguard.controller;

import com.finguard.entity.AuditLog;
import com.finguard.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page; // Import this
import org.springframework.data.domain.PageRequest;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuditLogController {

    private final AuditRepository auditRepository;
    @GetMapping
    public Page<AuditLog> getAllLogs(@RequestParam(defaultValue = "0") int page) {
        // Create a page request for the requested page with a size of 10
        return auditRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, 10));
    }
}