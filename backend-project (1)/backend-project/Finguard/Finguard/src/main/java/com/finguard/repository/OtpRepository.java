package com.finguard.repository;

import com.finguard.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
    // Finds the OTP record for a specific email
    Optional<OtpEntity> findByEmail(String email);

    // Deletes any existing OTP for an email so a user only has one active code
    void deleteByEmail(String email);
}