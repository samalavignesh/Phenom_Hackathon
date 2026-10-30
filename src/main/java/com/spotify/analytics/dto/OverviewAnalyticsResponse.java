package com.spotify.analytics.dto;

public class OverviewAnalyticsResponse {

    private String tenantId;
    private long totalPlays;
    private long uniqueListeners;
    private double totalListeningHours;
    private long totalLikes;
    private long totalSkips;
    private long totalSearches;
    private long totalPlaylistAdds;
    private double overallLikeRate;
    private double overallSkipRate;

    public OverviewAnalyticsResponse() {
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

    public long getTotalSearches() {
        return totalSearches;
    }

    public void setTotalSearches(long totalSearches) {
        this.totalSearches = totalSearches;
    }

    public long getTotalPlaylistAdds() {
        return totalPlaylistAdds;
    }

    public void setTotalPlaylistAdds(long totalPlaylistAdds) {
        this.totalPlaylistAdds = totalPlaylistAdds;
    }

    public double getOverallLikeRate() {
        return overallLikeRate;
    }

    public void setOverallLikeRate(double overallLikeRate) {
        this.overallLikeRate = overallLikeRate;
    }

    public double getSkipRate() {
        return overallSkipRate;
    }

    public void setOverallSkipRate(double overallSkipRate) {
        this.overallSkipRate = overallSkipRate;
    }
}
