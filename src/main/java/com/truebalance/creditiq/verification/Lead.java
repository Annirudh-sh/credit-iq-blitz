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
@Table(name = "h_lead_record")
@Getter
@Setter
public class Lead {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "game_attempt_id", nullable = false, length = 36)
    private String gameAttemptId;

    @Column(name = "intent_category", length = 10)
    private String intentCategory;

    @Column(name = "cibil_consent", nullable = false)
    private boolean cibilConsent;

    @Column(name = "comms_consent", nullable = false)
    private boolean commsConsent;

    @Column(name = "consent_at")
    private Instant consentAt;

    @Column(name = "consent_text", columnDefinition = "TEXT")
    private String consentText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }
}
