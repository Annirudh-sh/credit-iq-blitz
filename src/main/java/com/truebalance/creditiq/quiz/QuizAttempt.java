package com.truebalance.creditiq.quiz;

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
@Table(name = "h_quiz_attempt")
@Getter
@Setter
public class QuizAttempt {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "correct_count")
    private Integer correctCount;

    @Column(name = "time_taken_sec")
    private Double timeTakenSec;

    private Integer coins;

    @Column(name = "display_name", length = 50)
    private String displayName;

    @Column(nullable = false)
    private boolean verified;

    @PrePersist
    void generateId() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
