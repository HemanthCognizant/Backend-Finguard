package com.finguard.service;
import com.finguard.entity.Alert;
import com.finguard.entity.User;
import com.finguard.repository.AlertRepository;
import com.finguard.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class UserService {
    private final UserRepository repo;
    private final AlertRepository alertRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repo, AlertRepository alertRepo) {
        this.repo = repo;
        this.alertRepo = alertRepo;
    }
    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }
        public User authenticate(String email, String password) {
            User user = repo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!encoder.matches(password, user.getPassword())) {
                // Track failed attempts (Assumes you add 'failedAttempts' field to User entity)
                user.setFailedAttempts(user.getFailedAttempts() + 1);

                if (user.getFailedAttempts() >= 3) {
                    Alert alert = new Alert();
                    alert.setType("Suspicious Login Activity");
                    alert.setCustomer(user.getName());
                    alert.setSeverity("MEDIUM");
                    alertRepo.save(alert);
                }
                repo.save(user);
                throw new RuntimeException("Invalid credentials");
            }

            user.setFailedAttempts(0); // Reset on success
            repo.save(user);
            return user;
        }
}
