package com.truebalance.creditiq.cricket;

import com.truebalance.creditiq.common.BadRequestException;
import com.truebalance.creditiq.common.GameType;
import com.truebalance.creditiq.common.NotFoundException;
import com.truebalance.creditiq.config.AppProperties;
import com.truebalance.creditiq.cricket.dto.CricketStartResponse;
import com.truebalance.creditiq.cricket.dto.CricketSubmitRequest;
import com.truebalance.creditiq.cricket.dto.CricketSubmitResponse;
import com.truebalance.creditiq.quiz.DeviceInfo;
import com.truebalance.creditiq.quiz.DeviceInfoRepository;
import com.truebalance.creditiq.quiz.GameAttempt;
import com.truebalance.creditiq.quiz.GameAttemptRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class CricketService {

    private final GameAttemptRepository attemptRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final AppProperties appProperties;

    public CricketService(GameAttemptRepository attemptRepository,
                          DeviceInfoRepository deviceInfoRepository,
                          AppProperties appProperties) {
        this.attemptRepository = attemptRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.appProperties = appProperties;
    }

    public CricketStartResponse start(String deviceId, String deviceType, String deviceModel,
                                      String browserInfo, String ipAddress, Double lat, Double lng) {
        var attempt = new GameAttempt();
        attempt.setGameType(GameType.CRICKET.name());
        attempt.setStartedAt(Instant.now());
        attemptRepository.save(attempt);

        var device = new DeviceInfo();
        device.setGameAttemptId(attempt.getId());
        device.setDeviceId(deviceId);
        device.setDeviceType(deviceType);
        device.setDeviceModel(deviceModel);
        device.setBrowserInfo(browserInfo);
        device.setIpAddress(ipAddress);
        device.setUserLat(lat);
        device.setUserLng(lng);
        deviceInfoRepository.save(device);

        return new CricketStartResponse(attempt.getId(), appProperties.cricket().maxBalls());
    }

    public CricketSubmitResponse submit(String attemptId, CricketSubmitRequest request) {
        var attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new NotFoundException("Attempt not found"));

        if (attempt.getSubmittedAt() != null) {
            throw new BadRequestException("Already submitted");
        }

        int maxRuns = appProperties.cricket().maxBalls() * 6;
        int score = Math.max(0, Math.min(request.totalRuns(), maxRuns));
        int coins = GameType.CRICKET.toCoins(score);

        Instant now = Instant.now();
        double timeTakenSec = Duration.between(attempt.getStartedAt(), now).toMillis() / 1000.0;

        attempt.setSubmittedAt(now);
        attempt.setScore(score);
        attempt.setCoins(coins);
        attempt.setTimeTakenSec(timeTakenSec);
        attemptRepository.save(attempt);

        return new CricketSubmitResponse(coins, score, timeTakenSec);
    }
}
