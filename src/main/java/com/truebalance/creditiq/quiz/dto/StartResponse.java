package com.truebalance.creditiq.quiz.dto;

import java.util.List;

public record StartResponse(String attemptId, List<QuestionDto> questions) {}
