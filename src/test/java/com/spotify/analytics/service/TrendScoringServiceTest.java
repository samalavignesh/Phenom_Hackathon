package com.spotify.analytics.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TrendScoringServiceTest {

    private TrendScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new TrendScoringService();
    }

    @Test
    void testGrowthScoreWhenPreviousZero() {
        double score = scoringService.calculateGrowthScore(10, 0);
        assertTrue(score >= 50.0 && score <= 100.0);
    }

    @Test
    void testGrowthScoreWhenCurrentZero() {
        double score = scoringService.calculateGrowthScore(0, 10);
        assertEquals(0.0, score);
    }

    @Test
    void testGrowthScoreDoubling() {
        double score = scoringService.calculateGrowthScore(20, 10);
        assertEquals(100.0, score);
    }

    @Test
    void testGrowthScoreUnchanged() {
        double score = scoringService.calculateGrowthScore(10, 10);
        assertEquals(50.0, score);
    }

    @Test
    void testLikeRateScore() {
        double score = scoringService.calculateLikeRateScore(25, 100);
        assertEquals(25.0, score);
    }

    @Test
    void testLikeRateScoreZeroPlays() {
        double score = scoringService.calculateLikeRateScore(5, 0);
        assertEquals(0.0, score);
    }

    @Test
    void testEngagementScoreNeutral() {
        double score = scoringService.calculateEngagementScore(100, 10, 10, 0);
        assertEquals(50.0, score);
    }

    @Test
    void testEngagementScoreHighSkips() {
        double score = scoringService.calculateEngagementScore(100, 0, 50, 0);
        assertTrue(score < 50.0);
    }

    @Test
    void testTrendScoreCalculation() {
        TrendScoringService.ScoreBreakdown breakdown = scoringService.calculateTrendScore(
                100, 50, 80, 40, 40, 5, 20
        );

        assertTrue(breakdown.getTrendScore() > 50.0);
        assertTrue(breakdown.getTrendScore() <= 100.0);
        assertTrue(breakdown.getPlayGrowthScore() > 0);
        assertTrue(breakdown.getLikeRateScore() > 0);
        assertTrue(breakdown.getUniqueListenerGrowthScore() > 0);
        assertTrue(breakdown.getEngagementScore() > 0);
    }
}
