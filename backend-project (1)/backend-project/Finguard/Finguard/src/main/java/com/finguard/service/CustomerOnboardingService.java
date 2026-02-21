package com.finguard.service;

import com.finguard.entity.CustomerOnboarding;
import com.finguard.entity.User;
import com.finguard.entity.OtpEntity;
import com.finguard.repository.CustomerOnboardingRepository;
import com.finguard.repository.OtpRepository;
import com.finguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOnboardingService {

    private final UserRepository userRepository;
    private final CustomerOnboardingRepository repository;
    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    /**
     * Step 1: Generate OTP, Save to DB, and Send Email
     * Renamed to sendOtp to match common Controller naming
     */
    @Transactional
    public void sendOtp(String email) {
        try {
            // 1. Clear any previous OTPs for this email to avoid duplicates
            otpRepository.deleteByEmail(email);

            // 2. Generate 6-digit code
            String otp = String.format("%06d", new Random().nextInt(999999));

            // 3. Save to MySQL with 5-minute expiry
            OtpEntity otpEntity = new OtpEntity();
            otpEntity.setEmail(email);
            otpEntity.setOtp(otp);
            otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));
            otpRepository.save(otpEntity);

            // 4. Send Email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Finguard Onboarding Verification");
            message.setText("Your verification code is: " + otp + "\nThis code expires in 5 minutes.");

            mailSender.send(message);
            log.info("OTP sent successfully to {}", email);

        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Email service failed. Check your SMTP settings.");
        } catch (Exception e) {
            log.error("Error in OTP generation for {}: {}", email, e.getMessage());
            throw new RuntimeException("Internal server error during OTP process.");
        }
    }

    /**
     * Step 2: Verify the OTP provided by the user
     */
    public boolean verifyOtp(String email, String userOtp) {
        return otpRepository.findByEmail(email)
                .filter(dbOtp -> dbOtp.getOtp().equals(userOtp))
                .filter(dbOtp -> dbOtp.getExpiryTime().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    /**
     * Step 3: Proceed with creating the onboarding record
     */
    public CustomerOnboarding create(CustomerOnboarding customer) {
        customer.setApplicationId(
                "KYC" + UUID.randomUUID().toString().substring(0,5).toUpperCase()
        );
        customer.setStatus("PENDING");
        customer.setCreatedAt(LocalDateTime.now());
        return repository.save(customer);
    }

    public List<CustomerOnboarding> getAll() {
        return repository.findAll();
    }

    public CustomerOnboarding updateStatus(String applicationId, String status) {
        CustomerOnboarding customer = repository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + applicationId));
        customer.setStatus(status);
        return repository.save(customer);
    }

    public CustomerOnboarding findByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerOnboarding profile = repository.findByEmail(user.getEmail()).orElse(null);

        if (profile == null) {
            CustomerOnboarding fallback = new CustomerOnboarding();
            fallback.setFullName(user.getName());
            fallback.setApplicationId("PENDING");
            fallback.setStatus("NOT_STARTED");
            return fallback;
        }
        return profile;
    }
}