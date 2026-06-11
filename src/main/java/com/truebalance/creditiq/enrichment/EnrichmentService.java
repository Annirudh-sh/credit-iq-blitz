package com.truebalance.creditiq.enrichment;

import com.truebalance.creditiq.enrichment.integration.CibilClient;
import com.truebalance.creditiq.enrichment.integration.CrmClient;
import com.truebalance.creditiq.verification.Lead;
import com.truebalance.creditiq.verification.LeadRepository;
import com.truebalance.creditiq.verification.User;
import com.truebalance.creditiq.verification.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EnrichmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrichmentService.class);

    private final CibilClient cibilClient;
    private final CrmClient crmClient;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;

    public EnrichmentService(CibilClient cibilClient,
                             CrmClient crmClient,
                             UserRepository userRepository,
                             LeadRepository leadRepository) {
        this.cibilClient = cibilClient;
        this.crmClient = crmClient;
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
    }

    public void enrich(User user, Lead lead, int coins) {
        try {
            var result = cibilClient.check(user.getPhone());
            user.setDbMatchFlag(result.dbMatchFlag());
            user.setCibilBand(result.band());
            user.setCibilScore(result.score());
        } catch (Exception e) {
            log.warn("CIBIL check failed, defaulting to NEW: {}", e.getMessage());
            user.setDbMatchFlag("NEW");
            user.setCibilBand("UNKNOWN");
            user.setCibilScore(null);
        }
        userRepository.save(user);

        String category = categorize(user.getCibilScore(), user.getDbMatchFlag());
        lead.setIntentCategory(category);
        leadRepository.save(lead);

        try {
            crmClient.pushLead(user, lead);
        } catch (Exception e) {
            log.warn("CRM push failed: {}", e.getMessage());
        }
    }

    String categorize(Integer cibilScore, String dbMatch) {
        if ("DEFAULTER".equalsIgnoreCase(dbMatch)) {
            return "COLD";
        }
        if (cibilScore != null && cibilScore >= 750) {
            return "HOT";
        }
        if (cibilScore != null && cibilScore >= 650) {
            return "WARM";
        }
        return "COLD";
    }
}
