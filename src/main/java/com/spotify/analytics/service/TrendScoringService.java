package com.spotify.analytics.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TrendScoringService {

    public static class ScoreBreakdown {
        private final double playGrowthScore;
        private final double likeRateScore;
        private final double uniqueListenerGrowthScore;
        private final double engagementScore;
        private final double trendScore;

        public ScoreBreakdown(double playGrowthScore, double likeRateScore, 
                              double uniqueListenerGrowthScore, double engagementScore, double trendScore) {
            this.playGrowthScore = playGrowthScore;
            this.likeRateScore = likeRateScore;
            this.uniqueListenerGrowthScore = uniqueListenerGrowthScore;
            this.engagementScore = engagementScore;
            this.trendScore = trendScore;
        }

        public double getPlayGrowthScore() {
            return playGrowthScore;
        }

        public double getLikeRateScore() {
            return likeRateScore;
        }

        public double getUniqueListenerGrowthScore() {
            return uniqueListenerGrowthScore;
        }

        public double getEngagementScore() {
            return engagementScore;
        }

        public double getTrendScore() {
            return trendScore;
        }
    }

    public ScoreBreakdown calculateTrendScore(
            long currentPlays,
            long previousPlays,
            long currentListeners,
            long previousListeners,
            long totalLikes,
            long totalSkips,
            long playlistAdds
    ) {
        double playGrowthScore = calculateGrowthScore(currentPlays, previousPlays);
        double listenerGrowthScore = calculateGrowthScore(currentListeners, previousListeners);
        double likeRateScore = calculateLikeRateScore(totalLikes, currentPlays);
        double engagementScore = calculateEngagementScore(currentPlays, totalLikes, totalSkips, playlistAdds);

        double rawTrendScore = (0.35 * playGrowthScore)
                + (0.25 * likeRateScore)
                + (0.20 * listenerGrowthScore)
                + (0.20 * engagementScore);

        double finalTrendScore = round(Math.max(0.0, Math.min(100.0, rawTrendScore)));

        return new ScoreBreakdown(
                round(playGrowthScore),
                round(likeRateScore),
                round(listenerGrowthScore),
                round(engagementScore),
                finalTrendScore
        );
    }

    public double calculateGrowthScore(long current, long previous) {
        if (current <= 0) {
            return 0.0;
        }
        if (previous <= 0) {
            return Math.min(100.0, Math.max(50.0, 50.0 + (current * 2.0)));
        }

        double growthPercent = ((double) (current - previous) / previous) * 100.0;

        if (growthPercent >= 100.0) {
            return 100.0;
        } else if (growthPercent <= -100.0) {
            return 0.0;
        } else {
            return (growthPercent + 100.0) / 2.0;
        }
    }

    public double calculateLikeRateScore(long totalLikes, long totalPlays) {
        if (totalPlays <= 0) {
            return 0.0;
        }
        double likeRate = (double) totalLikes / totalPlays;
        return Math.min(100.0, Math.max(0.0, likeRate * 100.0));
    }

    public double calculateEngagementScore(long totalPlays, long totalLikes, long totalSkips, long playlistAdds) {
        if (totalPlays <= 0) {
            return 0.0;
        }
        double positiveActions = (totalLikes * 1.0) + (playlistAdds * 1.5);
        double negativeActions = totalSkips * 1.0;
        double netEngagement = (positiveActions - negativeActions) / totalPlays;

        double normalizedScore = 50.0 + (netEngagement * 50.0);
        return Math.min(100.0, Math.max(0.0, normalizedScore));
    }

    public double calculateGrowthPercent(long current, long previous) {
        if (previous <= 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return round(((double) (current - previous) / previous) * 100.0);
    }

    public double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
