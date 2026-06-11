package com.truebalance.creditiq.verification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "h_otp_code")
@Getter
@Setter
public class OtpCode {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean consumed;

    @PrePersist
    void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
