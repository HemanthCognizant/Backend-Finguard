package com.finguard.service;
import com.finguard.entity.Alert;
import com.finguard.entity.AuditLog;
import com.finguard.entity.User;
import com.finguard.repository.AlertRepository;
import com.finguard.repository.AuditRepository;
import com.finguard.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class UserService {
    private final UserRepository repo;
    private final AlertRepository alertRepo;
    private final AuditRepository auditRepo;
    private final HttpServletRequest request;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repo, AlertRepository alertRepo, AuditRepository auditRepo, HttpServletRequest request) {
        this.repo = repo;
        this.alertRepo = alertRepo;
        this.auditRepo=auditRepo;
        this.request=request;
    }
    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }
        public User authenticate(String email, String password) {
            User user = repo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
//            String ip = request.getRemoteAddr();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }

            if (!encoder.matches(password, user.getPassword())) {
                // Track failed attempts (Assumes you add 'failedAttempts' field to User entity)
                user.setFailedAttempts(user.getFailedAttempts() + 1);

                if (user.getFailedAttempts() >= 3) {
                    Alert alert = new Alert();
                    alert.setType("Suspicious Login Activity");
                    alert.setCustomer(user.getName());
                    alert.setSeverity("MEDIUM");
                    alertRepo.save(alert);

                    auditRepo.save(new AuditLog(user.getName(), "Suspicious Login", "Authentication", "Failed login limit reached", ip));
                }
                repo.save(user);
                throw new RuntimeException("Invalid credentials");
            }

            user.setFailedAttempts(0); // Reset on success
            repo.save(user);
            auditRepo.save(new AuditLog(user.getName(), "Login", "Authentication", "Successful login", ip));
            return user;
        }
}
