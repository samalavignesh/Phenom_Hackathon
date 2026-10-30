package com.spotify.analytics.dto;

import java.math.BigDecimal;

public class SongAnalyticsResponse {

    private String songId;
    private String artistId;
    private String tenantId;
    private long totalPlays;
    private long uniqueListeners;
    private double totalListeningHours;
    private long totalLikes;
    private long totalSkips;
    private double likeRate;
    private double skipRate;
    private long searchCount;
    private long playlistAdds;
    private long previousWindowPlays;
    private long previousWindowUniqueListeners;
    private double playGrowthPercent;
    private double playGrowthScore;
    private double likeRateScore;
    private double uniqueListenerGrowthScore;
    private double engagementScore;
    private double trendScore;
    private Integer rank;

    public SongAnalyticsResponse() {
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public long getTotalPlays() {
        return totalPlays;
    }

    public void setTotalPlays(long totalPlays) {
        this.totalPlays = totalPlays;
    }

    public long getUniqueListeners() {
        return uniqueListeners;
    }

    public void setUniqueListeners(long uniqueListeners) {
        this.uniqueListeners = uniqueListeners;
    }

    public double getTotalListeningHours() {
        return totalListeningHours;
    }

    public void setTotalListeningHours(double totalListeningHours) {
        this.totalListeningHours = totalListeningHours;
    }

    public long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public long getTotalSkips() {
        return totalSkips;
    }

    public void setTotalSkips(long totalSkips) {
        this.totalSkips = totalSkips;
    }

    public double getLikeRate() {
        return likeRate;
    }

    public void setLikeRate(double likeRate) {
        this.likeRate = likeRate;
    }

    public double getSkipRate() {
        return skipRate;
    }

    public void setSkipRate(double skipRate) {
        this.skipRate = skipRate;
    }

    public long getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(long searchCount) {
        this.searchCount = searchCount;
    }

    public long getPlaylistAdds() {
        return playlistAdds;
    }

    public void setPlaylistAdds(long playlistAdds) {
        this.playlistAdds = playlistAdds;
    }

    public long getPreviousWindowPlays() {
        return previousWindowPlays;
    }

    public void setPreviousWindowPlays(long previousWindowPlays) {
        this.previousWindowPlays = previousWindowPlays;
    }

    public long getPreviousWindowUniqueListeners() {
        return previousWindowUniqueListeners;
    }

    public void setPreviousWindowUniqueListeners(long previousWindowUniqueListeners) {
        this.previousWindowUniqueListeners = previousWindowUniqueListeners;
    }

    public double getPlayGrowthPercent() {
        return playGrowthPercent;
    }

    public void setPlayGrowthPercent(double playGrowthPercent) {
        this.playGrowthPercent = playGrowthPercent;
    }

    public double getPlayGrowthScore() {
        return playGrowthScore;
    }

    public void setPlayGrowthScore(double playGrowthScore) {
        this.playGrowthScore = playGrowthScore;
    }

    public double getLikeRateScore() {
        return likeRateScore;
    }

    public void setLikeRateScore(double likeRateScore) {
        this.likeRateScore = likeRateScore;
    }

    public double getUniqueListenerGrowthScore() {
        return uniqueListenerGrowthScore;
    }

    public void setUniqueListenerGrowthScore(double uniqueListenerGrowthScore) {
        this.uniqueListenerGrowthScore = uniqueListenerGrowthScore;
    }

    public double getEngagementScore() {
        return engagementScore;
    }

    public void setEngagementScore(double engagementScore) {
        this.engagementScore = engagementScore;
    }

    public double getTrendScore() {
        return trendScore;
    }

    public void setTrendScore(double trendScore) {
        this.trendScore = trendScore;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
