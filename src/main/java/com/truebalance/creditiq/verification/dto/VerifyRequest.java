package com.truebalance.creditiq.verification.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyRequest(
        @NotBlank String phone,
        @NotBlank String code,
        @NotBlank String attemptId,
        String name,
        boolean cibilConsent,
        boolean commsConsent
) {}
