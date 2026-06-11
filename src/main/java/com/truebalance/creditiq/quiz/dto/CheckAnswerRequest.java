package com.truebalance.creditiq.quiz.dto;

import jakarta.validation.constraints.NotNull;

public record CheckAnswerRequest(
        @NotNull String questionId,
        @NotNull Integer selectedIndex
) {}
