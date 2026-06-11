package com.truebalance.creditiq.enrichment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "h_city_device_score")
@Getter
@Setter
public class CityDeviceScore {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "device_model", nullable = false, length = 100)
    private String deviceModel;

    @Column(nullable = false)
    private int score;

    @PrePersist
    void generateId() {
        if (id == null) id = UUID.randomUUID().toString();
    }
}
