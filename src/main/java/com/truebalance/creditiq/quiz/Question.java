package com.truebalance.creditiq.quiz;

import java.util.List;

public record Question(String id, String text, List<String> options, int correctIndex) {}
