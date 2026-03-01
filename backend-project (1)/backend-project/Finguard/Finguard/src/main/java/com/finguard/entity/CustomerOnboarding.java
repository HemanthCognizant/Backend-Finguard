package com.finguard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_onboarding")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CustomerOnboarding {
    @Id
    @Column(unique = true)
    private String applicationId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String mobile;
    @Column(unique = true, nullable = false)
    private String email;
    private String aadhaarFront;
    private String aadhaarBack;
    private String panCard;
    private String photo;
    private String status;
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private Double balance=(250000.0);
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.applicationId == null || this.applicationId.isEmpty()) {
            this.applicationId = "KYC-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (this.status == null) {
            this.status = "Pending";
        }
    }

}
