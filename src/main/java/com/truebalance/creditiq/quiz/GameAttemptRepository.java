package com.truebalance.creditiq.quiz;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameAttemptRepository extends JpaRepository<GameAttempt, String> {

    @Query("SELECT a FROM GameAttempt a WHERE a.verified = true AND a.gameType = :gameType ORDER BY a.coins DESC, a.timeTakenSec ASC")
    List<GameAttempt> findTopVerifiedByGame(@Param("gameType") String gameType, Pageable pageable);

    @Query("SELECT COUNT(a) FROM GameAttempt a WHERE a.verified = true AND a.gameType = :gameType " +
           "AND (a.coins > :coins OR (a.coins = :coins AND a.timeTakenSec < :time))")
    long countRankedAbove(@Param("gameType") String gameType, @Param("coins") int coins, @Param("time") double time);

    long countByVerifiedTrueAndGameType(String gameType);
}
