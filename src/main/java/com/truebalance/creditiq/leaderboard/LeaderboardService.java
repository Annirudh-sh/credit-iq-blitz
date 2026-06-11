package com.truebalance.creditiq.leaderboard;

import com.truebalance.creditiq.config.AppProperties;
import com.truebalance.creditiq.leaderboard.dto.LeaderboardEntry;
import com.truebalance.creditiq.quiz.QuizAttemptRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    private final QuizAttemptRepository attemptRepository;
    private final AppProperties appProperties;

    public LeaderboardService(QuizAttemptRepository attemptRepository,
                              AppProperties appProperties) {
        this.attemptRepository = attemptRepository;
        this.appProperties = appProperties;
    }

    public List<LeaderboardEntry> getTop() {
        var attempts = attemptRepository.findTopVerified(
                PageRequest.of(0, appProperties.leaderboard().size()));
        var entries = new ArrayList<LeaderboardEntry>();
        for (int i = 0; i < attempts.size(); i++) {
            var a = attempts.get(i);
            String name = a.getDisplayName() != null ? a.getDisplayName() : "Player";
            entries.add(new LeaderboardEntry(i + 1, name, a.getCoins()));
        }
        return entries;
    }
}
