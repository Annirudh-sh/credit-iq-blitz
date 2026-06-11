package com.truebalance.creditiq.quiz;

import com.truebalance.creditiq.quiz.dto.StartResponse;
import com.truebalance.creditiq.quiz.dto.SubmitRequest;
import com.truebalance.creditiq.quiz.dto.SubmitResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/start")
    public StartResponse start(
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Device-Type", required = false) String deviceType,
            @RequestHeader(value = "X-Device-Model", required = false) String deviceModel,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude,
            HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        String browserInfo = request.getHeader("User-Agent");
        return quizService.start(deviceId, deviceType, deviceModel, browserInfo, ip, latitude, longitude);
    }

    @PostMapping("/{attemptId}/submit")
    public SubmitResponse submit(@PathVariable String attemptId,
                                 @RequestBody @Valid SubmitRequest request) {
        return quizService.submit(attemptId, request);
    }
}
