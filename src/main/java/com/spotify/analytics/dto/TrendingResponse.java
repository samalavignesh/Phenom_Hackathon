package com.spotify.analytics.dto;

import java.util.List;

public class TrendingResponse {

    private String tenantId;
    private String window;
    private int count;
    private List<SongAnalyticsResponse> trendingSongs;

    public TrendingResponse() {
    }

    public TrendingResponse(String tenantId, String window, int count, List<SongAnalyticsResponse> trendingSongs) {
        this.tenantId = tenantId;
        this.window = window;
        this.count = count;
        this.trendingSongs = trendingSongs;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getWindow() {
        return window;
    }

    public void setWindow(String window) {
        this.window = window;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<SongAnalyticsResponse> getTrendingSongs() {
        return trendingSongs;
    }

    public void setTrendingSongs(List<SongAnalyticsResponse> trendingSongs) {
        this.trendingSongs = trendingSongs;
    }
}
