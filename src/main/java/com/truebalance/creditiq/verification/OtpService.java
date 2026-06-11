package com.truebalance.creditiq.verification;

import com.truebalance.creditiq.common.BadRequestException;
import com.truebalance.creditiq.common.NotFoundException;
import com.truebalance.creditiq.config.AppProperties;
import com.truebalance.creditiq.enrichment.EnrichmentService;
import com.truebalance.creditiq.quiz.DeviceInfo;
import com.truebalance.creditiq.quiz.DeviceInfoRepository;
import com.truebalance.creditiq.quiz.GameAttempt;
import com.truebalance.creditiq.quiz.GameAttemptRepository;
import com.truebalance.creditiq.verification.dto.VerifyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final String CONSENT_TEXT =
            "[Comms] I agree to Terms of Service (https://www.truebalance.io/terms-and-conditions/service) " +
            "and Privacy Policy (https://www.truebalance.io/terms-and-conditions/privacy-policy). " +
            "I share my consent to True Balance to access/collect my phone number and SMS and to receive " +
            "communications including promotional offers via SMS, WhatsApp & email. " +
            "[CIBIL] I hereby provide my explicit consent to True Credits Private Limited ('the Company') to " +
            "fetch my credit report from TUCIBIL to assess my creditworthiness. I also agree to TUCIBIL User " +
            "Consent terms and Company's Terms and Conditions. Share my credit report with its Lending Partners " +
            "and Service Providers solely to evaluate my eligibility for financial products.";

    private final OtpCodeRepository otpRepository;
    private final UserRepository userRepository;
    private final LeadRepository leadRepository;
    private final GameAttemptRepository attemptRepository;
    private final DeviceInfoRepository deviceInfoRepository;
    private final EnrichmentService enrichmentService;
    private final AppProperties appProperties;

    public OtpService(OtpCodeRepository otpRepository,
                      UserRepository userRepository,
                      LeadRepository leadRepository,
                      GameAttemptRepository attemptRepository,
                      DeviceInfoRepository deviceInfoRepository,
                      EnrichmentService enrichmentService,
                      AppProperties appProperties) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.leadRepository = leadRepository;
        this.attemptRepository = attemptRepository;
        this.deviceInfoRepository = deviceInfoRepository;
        this.enrichmentService = enrichmentService;
        this.appProperties = appProperties;
    }

    public void requestOtp(String phone) {
        String code = generateCode();

        var otp = new OtpCode();
        otp.setPhone(phone);
        otp.setCode(code);
        otp.setExpiresAt(Instant.now().plus(appProperties.otp().ttlMinutes(), ChronoUnit.MINUTES));
        otpRepository.save(otp);

        String masked = "****" + phone.substring(Math.max(0, phone.length() - 4));
        log.info("OTP generated for {}: {}", masked, code);
    }

    @Transactional
    public VerifyResponse verify(String phone, String code, String attemptId,
                                 String name, boolean cibilConsent, boolean commsConsent) {
        // OTP bypass: accept any code for demo
        var otp = otpRepository
                .findFirstByPhoneAndConsumedFalseAndExpiresAtAfterOrderByExpiresAtDesc(phone, Instant.now())
                .orElse(null);
        if (otp != null) {
            otp.setConsumed(true);
            otpRepository.save(otp);
        }

        GameAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new NotFoundException("Attempt not found"));

        if (attempt.getCoins() == null) {
            throw new BadRequestException("Quiz not submitted yet");
        }

        // Find or create user
        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            var u = new User();
            u.setPhone(phone);
            return u;
        });
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (attempt.getCoins() > user.getTbCoins()) {
            user.setTbCoins(attempt.getCoins());
        }
        userRepository.save(user);

        // Link attempt to user
        attempt.setUserId(user.getId());
        attempt.setVerified(true);
        if (name != null && !name.isBlank()) {
            attempt.setDisplayName(name);
        }
        attemptRepository.save(attempt);

        // Create lead record
        var lead = new Lead();
        lead.setUserId(user.getId());
        lead.setGameAttemptId(attempt.getId());
        lead.setCibilConsent(cibilConsent);
        lead.setCommsConsent(commsConsent);
        lead.setConsentAt(Instant.now());
        lead.setConsentText(CONSENT_TEXT);
        leadRepository.save(lead);

        // Lookup device info for city + model
        String city = null;
        String deviceModel = null;
        var deviceOpt = deviceInfoRepository.findByGameAttemptId(attempt.getId());
        if (deviceOpt.isPresent()) {
            city = deviceOpt.get().getCity();
            deviceModel = deviceOpt.get().getDeviceModel();
        }

        // Enrichment (CIBIL check, categorize, CRM push)
        enrichmentService.enrich(user, lead, attempt.getCoins(), city, deviceModel);

        long above = attemptRepository.countRankedAbove(attempt.getGameType(), attempt.getCoins(), attempt.getTimeTakenSec());
        int rank = (int) above + 1;
        long totalPlayers = attemptRepository.countByVerifiedTrueAndGameType(attempt.getGameType());

        return new VerifyResponse(rank, attempt.getCoins(), totalPlayers);
    }

    private String generateCode() {
        String fixed = appProperties.otp().fixedCode();
        if (fixed != null && !fixed.isBlank()) {
            return fixed;
        }
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
