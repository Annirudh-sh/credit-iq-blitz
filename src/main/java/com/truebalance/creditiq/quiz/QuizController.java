package com.truebalance.creditiq.quiz;

import com.truebalance.creditiq.quiz.dto.CheckAnswerRequest;
import com.truebalance.creditiq.quiz.dto.CheckAnswerResponse;
import com.truebalance.creditiq.quiz.dto.StartResponse;
import com.truebalance.creditiq.quiz.dto.SubmitRequest;
import com.truebalance.creditiq.quiz.dto.SubmitResponse;
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
    private final QuestionService questionService;

    public QuizController(QuizService quizService, QuestionService questionService) {
        this.quizService = quizService;
        this.questionService = questionService;
    }

    @PostMapping("/start")
    public StartResponse start(
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Device-Model", required = false) String deviceModel,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude) {
        return quizService.start(deviceId, deviceModel, latitude, longitude);
    }

    @PostMapping("/check")
    public CheckAnswerResponse check(@RequestBody @Valid CheckAnswerRequest request) {
        int correctIndex = questionService.getCorrectIndex(request.questionId());
        boolean correct = (correctIndex == request.selectedIndex());
        return new CheckAnswerResponse(correct, correctIndex);
    }

    @PostMapping("/{attemptId}/submit")
    public SubmitResponse submit(@PathVariable String attemptId,
                                 @RequestBody @Valid SubmitRequest request) {
        return quizService.submit(attemptId, request);
    }
}
