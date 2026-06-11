package com.truebalance.creditiq.cricket.dto;

import jakarta.validation.constraints.Min;

public record CricketSubmitRequest(@Min(0) int totalRuns) {}
