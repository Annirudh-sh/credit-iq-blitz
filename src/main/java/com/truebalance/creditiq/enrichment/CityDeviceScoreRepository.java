package com.truebalance.creditiq.enrichment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityDeviceScoreRepository extends JpaRepository<CityDeviceScore, String> {

    Optional<CityDeviceScore> findByCityAndDeviceModel(String city, String deviceModel);
}
