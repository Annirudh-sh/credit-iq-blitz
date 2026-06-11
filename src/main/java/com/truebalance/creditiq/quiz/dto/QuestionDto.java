package com.truebalance.creditiq.quiz.dto;

import java.util.List;

public record QuestionDto(String id, String text, List<String> options, int correctIndex) {}
