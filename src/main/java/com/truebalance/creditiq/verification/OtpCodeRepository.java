package com.truebalance.creditiq.verification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, String> {

    Optional<OtpCode> findFirstByPhoneAndConsumedFalseAndExpiresAtAfterOrderByExpiresAtDesc(
            String phone, Instant now);
}
