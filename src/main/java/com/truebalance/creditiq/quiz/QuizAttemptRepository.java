package com.truebalance.creditiq.quiz;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, String> {

    @Query("SELECT a FROM QuizAttempt a WHERE a.verified = true ORDER BY a.coins DESC, a.timeTakenSec ASC")
    List<QuizAttempt> findTopVerified(Pageable pageable);

    @Query("SELECT COUNT(a) FROM QuizAttempt a WHERE a.verified = true " +
           "AND (a.coins > :coins OR (a.coins = :coins AND a.timeTakenSec < :time))")
    long countRankedAbove(@Param("coins") int coins, @Param("time") double time);

    long countByVerifiedTrue();
}
