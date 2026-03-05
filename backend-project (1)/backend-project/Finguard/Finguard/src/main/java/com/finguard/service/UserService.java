package com.finguard.service;
import com.finguard.entity.Alert;
import com.finguard.entity.AuditLog;
import com.finguard.entity.User;
import com.finguard.repository.AlertRepository;
import com.finguard.repository.AuditRepository;
import com.finguard.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final AuditRepository auditRepository;
    private final HttpServletRequest request;
    private final PasswordEncoder encoder;

    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
        public User authenticate(String email, String password) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }

            if (!encoder.matches(password, user.getPassword())) {
                user.setFailedAttempts(user.getFailedAttempts() + 1);
                if (user.getFailedAttempts() >= 3) {
                    Alert alert = new Alert();
                    alert.setType("Suspicious Login Activity");
                    alert.setCustomer(user.getName());
                    alert.setSeverity("MEDIUM");
                    alertRepository.save(alert);
                    auditRepository.save(new AuditLog(user.getEmail(),
                            user.getRole().toString(),
                            "Suspicious Login",
                            "Authentication",
                            "Failed login limit reached",
                            ip));
                }
                userRepository.save(user);
                throw new RuntimeException("Invalid credentials");
            }

            user.setFailedAttempts(0);
            userRepository.save(user);
            auditRepository.save(new AuditLog(user.getEmail(),
                    user.getRole().toString(),
                    "Login",
                    "Authentication",
                    "Successful login",
                    ip));
            return user;
        }
}
