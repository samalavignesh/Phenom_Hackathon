package com.spotify.analytics.dto;

public class SearchTrendResponse {

    private String songId;
    private String artistId;
    private long searchCount;
    private Integer rank;

    public SearchTrendResponse() {
    }

    public SearchTrendResponse(String songId, String artistId, long searchCount, Integer rank) {
        this.songId = songId;
        this.artistId = artistId;
        this.searchCount = searchCount;
        this.rank = rank;
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

    public long getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(long searchCount) {
        this.searchCount = searchCount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
