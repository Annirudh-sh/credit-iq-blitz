package com.truebalance.creditiq.cricket;

import com.truebalance.creditiq.cricket.dto.CricketStartResponse;
import com.truebalance.creditiq.cricket.dto.CricketSubmitRequest;
import com.truebalance.creditiq.cricket.dto.CricketSubmitResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cricket")
public class CricketController {

    private final CricketService cricketService;

    public CricketController(CricketService cricketService) {
        this.cricketService = cricketService;
    }

    @PostMapping("/start")
    public CricketStartResponse start(
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Device-Model", required = false) String deviceModel,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude) {
        return cricketService.start(deviceId, deviceModel, latitude, longitude);
    }

    @PostMapping("/{attemptId}/submit")
    public CricketSubmitResponse submit(@PathVariable String attemptId,
                                        @RequestBody @Valid CricketSubmitRequest request) {
        return cricketService.submit(attemptId, request);
    }
}
