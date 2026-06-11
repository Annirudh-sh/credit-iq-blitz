package com.truebalance.creditiq.quiz;

import com.truebalance.creditiq.common.BadRequestException;
import com.truebalance.creditiq.common.NotFoundException;
import com.truebalance.creditiq.config.AppProperties;
import com.truebalance.creditiq.quiz.dto.StartResponse;
import com.truebalance.creditiq.quiz.dto.SubmitRequest;
import com.truebalance.creditiq.quiz.dto.SubmitResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class QuizService {

    private final QuizAttemptRepository attemptRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final QuestionService questionService;
    private final AppProperties appProperties;

    public QuizService(QuizAttemptRepository attemptRepository,
                       DeviceInfoRepository deviceInfoRepository,
                       QuestionService questionService,
                       AppProperties appProperties) {
        this.attemptRepository = attemptRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.questionService = questionService;
        this.appProperties = appProperties;
    }

    public StartResponse start(String deviceId, String deviceType, String deviceModel,
                               String browserInfo, String ipAddress, Double lat, Double lng) {
        var attempt = new QuizAttempt();
        attempt.setStartedAt(Instant.now());
        attemptRepository.save(attempt);

        var device = new DeviceInfo();
        device.setQuizAttemptId(attempt.getId());
        device.setDeviceId(deviceId);
        device.setDeviceType(deviceType);
        device.setDeviceModel(deviceModel);
        device.setBrowserInfo(browserInfo);
        device.setIpAddress(ipAddress);
        device.setUserLat(lat);
        device.setUserLng(lng);
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
        int correctCount = questionService.score(request.answers());
        int coins = correctCount * appProperties.quiz().coinsPerQuestion();
        double timeTakenSec = Duration.between(attempt.getStartedAt(), now).toMillis() / 1000.0;

        attempt.setSubmittedAt(now);
        attempt.setCorrectCount(correctCount);
        attempt.setCoins(coins);
        attempt.setTimeTakenSec(timeTakenSec);
        attemptRepository.save(attempt);

        return new SubmitResponse(coins, correctCount, timeTakenSec);
    }
}
