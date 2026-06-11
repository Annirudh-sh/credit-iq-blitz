package com.truebalance.creditiq.quiz;

import com.truebalance.creditiq.common.BadRequestException;
import com.truebalance.creditiq.common.GameType;
import com.truebalance.creditiq.common.NotFoundException;
import com.truebalance.creditiq.quiz.dto.StartResponse;
import com.truebalance.creditiq.quiz.dto.SubmitRequest;
import com.truebalance.creditiq.quiz.dto.SubmitResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class QuizService {

    private final GameAttemptRepository attemptRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final QuestionService questionService;
    private final GeoService geoService;

    public QuizService(GameAttemptRepository attemptRepository,
                       DeviceInfoRepository deviceInfoRepository,
                       QuestionService questionService,
                       GeoService geoService) {
        this.attemptRepository = attemptRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.questionService = questionService;
        this.geoService = geoService;
    }

    public StartResponse start(String deviceId, String deviceModel, Double lat, Double lng) {
        var attempt = new GameAttempt();
        attempt.setGameType(GameType.CREDIT_IQ.name());
        attempt.setStartedAt(Instant.now());
        attemptRepository.save(attempt);

        var device = new DeviceInfo();
        device.setGameAttemptId(attempt.getId());
        device.setDeviceId(deviceId);
        device.setDeviceModel(deviceModel);
        device.setUserLat(lat);
        device.setUserLng(lng);
        device.setCity(geoService.resolveCity(lat, lng));
        deviceInfoRepository.save(device);

        return new StartResponse(attempt.getId(), questionService.getAllForClient());
    }

    public SubmitResponse submit(String attemptId, SubmitRequest request) {
        var attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new NotFoundException("Attempt not found"));

        if (attempt.getSubmittedAt() != null) {
            throw new BadRequestException("Already submitted");
        }

        Instant now = Instant.now();
        int score = questionService.score(request.answers());
        int coins = GameType.CREDIT_IQ.toCoins(score);
        double timeTakenSec = Duration.between(attempt.getStartedAt(), now).toMillis() / 1000.0;

        attempt.setSubmittedAt(now);
        attempt.setScore(score);
        attempt.setCoins(coins);
        attempt.setTimeTakenSec(timeTakenSec);
        attemptRepository.save(attempt);

        return new SubmitResponse(coins, score, timeTakenSec);
    }
}
