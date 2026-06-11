package com.truebalance.creditiq.quiz;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "h_device_info")
@Getter
@Setter
public class DeviceInfo {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "game_attempt_id", nullable = false, length = 36)
    private String gameAttemptId;

    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(name = "device_model", length = 100)
    private String deviceModel;

    @Column(name = "user_lat")
    private Double userLat;

    @Column(name = "user_lng")
    private Double userLng;

    @Column(name = "city", length = 100)
    private String city;

    @PrePersist
    void generateId() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
