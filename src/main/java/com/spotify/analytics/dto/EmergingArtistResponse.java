package com.spotify.analytics.dto;

public class EmergingArtistResponse {

    private String artistId;
    private long currentPlays;
    private long previousPlays;
    private long currentUniqueListeners;
    private long previousUniqueListeners;
    private double playGrowthPercent;
    private double listenerGrowthPercent;
    private double engagementRate;
    private double emergingScore;
    private Integer rank;

    public EmergingArtistResponse() {
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public long getCurrentPlays() {
        return currentPlays;
    }

    public void setCurrentPlays(long currentPlays) {
        this.currentPlays = currentPlays;
    }

    public long getPreviousPlays() {
        return previousPlays;
    }

    public void setPreviousPlays(long previousPlays) {
        this.previousPlays = previousPlays;
    }

    public long getCurrentUniqueListeners() {
        return currentUniqueListeners;
    }

    public void setCurrentUniqueListeners(long currentUniqueListeners) {
        this.currentUniqueListeners = currentUniqueListeners;
    }

    public long getPreviousUniqueListeners() {
        return previousUniqueListeners;
    }

    public void setPreviousUniqueListeners(long previousUniqueListeners) {
        this.previousUniqueListeners = previousUniqueListeners;
    }

    public double getPlayGrowthPercent() {
        return playGrowthPercent;
    }

    public void setPlayGrowthPercent(double playGrowthPercent) {
        this.playGrowthPercent = playGrowthPercent;
    }

    public double getListenerGrowthPercent() {
        return listenerGrowthPercent;
    }

    public void setListenerGrowthPercent(double listenerGrowthPercent) {
        this.listenerGrowthPercent = listenerGrowthPercent;
    }

    public double getEngagementRate() {
        return engagementRate;
    }

    public void setEngagementRate(double engagementRate) {
        this.engagementRate = engagementRate;
    }

    public double getEmergingScore() {
        return emergingScore;
    }

    public void setEmergingScore(double emergingScore) {
        this.emergingScore = emergingScore;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
