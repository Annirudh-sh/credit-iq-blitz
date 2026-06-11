package com.truebalance.creditiq.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, String> {

    Optional<DeviceInfo> findByGameAttemptId(String gameAttemptId);
}
