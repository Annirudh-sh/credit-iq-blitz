package com.truebalance.creditiq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Quiz quiz,
        Cricket cricket,
        Leaderboard leaderboard,
        Otp otp,
        Integrations integrations
) {
    public record Quiz(int coinsPerQuestion) {}

    public record Cricket(int coinsPerRun, int maxBalls) {}

    public record Leaderboard(int size) {}

    public record Otp(String fixedCode, int ttlMinutes) {}

    public record Integrations(
            CibilConfig cibil,
            ExperianConfig experian,
            CrmConfig crm
    ) {
        public record CibilConfig(String mode, String baseUrl, int timeoutMs) {}
        public record ExperianConfig(String mode, String baseUrl) {}
        public record CrmConfig(String mode, String baseUrl) {}
    }
}
