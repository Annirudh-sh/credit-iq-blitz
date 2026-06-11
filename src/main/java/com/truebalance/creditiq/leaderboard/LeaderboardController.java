package com.truebalance.creditiq.leaderboard;

import com.truebalance.creditiq.leaderboard.dto.LeaderboardEntry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public List<LeaderboardEntry> get(@RequestParam(defaultValue = "CREDIT_IQ") String gameType) {
        return leaderboardService.getTop(gameType);
    }
}
