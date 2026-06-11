package com.truebalance.creditiq.enrichment.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.integrations.cibil.mode", havingValue = "mock", matchIfMissing = true)
public class MockCibilClient implements CibilClient {

    private static final Logger log = LoggerFactory.getLogger(MockCibilClient.class);

    @Override
    public CibilResult check(String phone) {
        String masked = "****" + phone.substring(Math.max(0, phone.length() - 4));
        int mockScore = 750 + (Math.abs(phone.hashCode()) % 100);
        String band = mockScore >= 750 ? "EXCELLENT" : mockScore >= 650 ? "GOOD" : "POOR";
        log.info("Mock CIBIL check for {}: score={}, band={}, dbMatch=NEW", masked, mockScore, band);
        return new CibilResult(mockScore, band, "NEW");
    }
}
