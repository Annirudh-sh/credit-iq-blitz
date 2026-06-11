package com.truebalance.creditiq.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitRequest(
        @NotEmpty @Valid List<AnswerDto> answers
) {}
