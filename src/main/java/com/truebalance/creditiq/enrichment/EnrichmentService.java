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

    private static final double CIBIL_WEIGHT = 0.70;
    private static final double CITY_DEVICE_WEIGHT = 0.30;

    private static final double HOT_THRESHOLD = 7.0;
    private static final double WARM_THRESHOLD = 4.5;

    private final CibilClient cibilClient;
    private final CrmClient crmClient;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final CityDeviceScoreRepository cityDeviceScoreRepository;

    public EnrichmentService(CibilClient cibilClient,
                             CrmClient crmClient,
                             UserRepository userRepository,
                             LeadRepository leadRepository,
                             CityDeviceScoreRepository cityDeviceScoreRepository) {
        this.cibilClient = cibilClient;
        this.crmClient = crmClient;
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.cityDeviceScoreRepository = cityDeviceScoreRepository;
    }

    public void enrich(User user, Lead lead, int coins, String city, String deviceModel) {
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

        String category = categorize(user.getCibilScore(), user.getDbMatchFlag(), city, deviceModel);
        lead.setIntentCategory(category);
        leadRepository.save(lead);

        try {
            crmClient.pushLead(user, lead);
        } catch (Exception e) {
            log.warn("CRM push failed: {}", e.getMessage());
        }
    }

    /**
     * Weighted rule engine:
     *   compositeScore = (cibilNorm * 0.70) + (cityDeviceScore * 0.30)
     *
     * CIBIL normalized to 0–10 scale:
     *   300–900 range → (score - 300) / 60
     *
     * City x Device score: looked up from h_city_device_score (0–10),
     *   defaults to 5 if no match found.
     *
     * Thresholds:
     *   >= 7.0 → HOT
     *   >= 4.5 → WARM
     *   < 4.5  → COLD
     *   DEFAULTER → always COLD
     */
    String categorize(Integer cibilScore, String dbMatch, String city, String deviceModel) {
        if ("DEFAULTER".equalsIgnoreCase(dbMatch)) {
            log.info("Rule engine: DEFAULTER -> COLD");
            return "COLD";
        }

        double cibilNorm = normalizeCibil(cibilScore);
        double cityDeviceNorm = lookupCityDeviceScore(city, deviceModel);
        double composite = (cibilNorm * CIBIL_WEIGHT) + (cityDeviceNorm * CITY_DEVICE_WEIGHT);

        String category;
        if (composite >= HOT_THRESHOLD) {
            category = "HOT";
        } else if (composite >= WARM_THRESHOLD) {
            category = "WARM";
        } else {
            category = "COLD";
        }

        log.info("Rule engine: cibil={} (norm={}), city={}, device={}, cityDeviceScore={}, composite={} -> {}",
                cibilScore, String.format("%.1f", cibilNorm), city, deviceModel,
                String.format("%.1f", cityDeviceNorm), String.format("%.2f", composite), category);

        return category;
    }

    double normalizeCibil(Integer cibilScore) {
        if (cibilScore == null) return 5.0;
        double clamped = Math.max(300, Math.min(900, cibilScore));
        return (clamped - 300) / 60.0;
    }

    double lookupCityDeviceScore(String city, String deviceModel) {
        if (city == null || deviceModel == null) return 5.0;
        return cityDeviceScoreRepository
                .findByCityAndDeviceModel(city, deviceModel)
                .map(s -> (double) s.getScore())
                .orElse(5.0);
    }
}
