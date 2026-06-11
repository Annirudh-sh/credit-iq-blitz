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
@Table(name = "h_user")
@Getter
@Setter
public class User {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Column(length = 50)
    private String name;

    @Column(name = "tb_coins", nullable = false)
    private int tbCoins;

    @Column(name = "cibil_score")
    private Integer cibilScore;

    @Column(name = "cibil_band", length = 20)
    private String cibilBand;

    @Column(name = "db_match_flag", length = 20)
    private String dbMatchFlag;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }
}
