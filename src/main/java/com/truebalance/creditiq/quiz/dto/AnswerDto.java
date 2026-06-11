package com.truebalance.creditiq.quiz.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerDto(
        @NotNull String questionId,
        @NotNull Integer selectedIndex
) {}
