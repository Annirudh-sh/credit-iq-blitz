package com.truebalance.creditiq.enrichment.integration;

import com.truebalance.creditiq.verification.Lead;
import com.truebalance.creditiq.verification.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.integrations.crm.mode", havingValue = "log", matchIfMissing = true)
public class LoggingCrmClient implements CrmClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingCrmClient.class);

    @Override
    public void pushLead(User user, Lead lead) {
        String masked = "****" + user.getPhone().substring(Math.max(0, user.getPhone().length() - 4));
        log.info("CRM push: user={}, phone={}, category={}, coins={}, cibilScore={}, cibilBand={}",
                user.getId(), masked, lead.getIntentCategory(),
                user.getTbCoins(), user.getCibilScore(), user.getCibilBand());
    }
}
