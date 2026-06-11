package com.truebalance.creditiq.quiz.dto;

import java.util.List;
import java.util.Map;

public record SubmitResponse(int coins, int score, double timeTakenSec, Map<String, Integer> correctAnswers) {}
