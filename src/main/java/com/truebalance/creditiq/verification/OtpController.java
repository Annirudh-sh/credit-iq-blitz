package com.truebalance.creditiq.verification;

import com.truebalance.creditiq.verification.dto.OtpRequest;
import com.truebalance.creditiq.verification.dto.VerifyRequest;
import com.truebalance.creditiq.verification.dto.VerifyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void request(@RequestBody @Valid OtpRequest request) {
        otpService.requestOtp(request.phone());
    }

    @PostMapping("/verify")
    public VerifyResponse verify(@RequestBody @Valid VerifyRequest request) {
        return otpService.verify(
                request.phone(),
                request.code(),
                request.attemptId(),
                request.name(),
                request.cibilConsent(),
                request.commsConsent());
    }
}
