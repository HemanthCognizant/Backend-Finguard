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

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(unique = true)

    private String applicationId;

    private String fullName;

    private LocalDate dateOfBirth;

    private String gender;

    private String address;

    private String mobile;

    private String email;

    private String aadhaarFront;

    private String aadhaarBack;

    private String panCard;

    private String photo;

    private String status;

    private LocalDateTime createdAt;

}
