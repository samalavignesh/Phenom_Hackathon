package com.spotify.analytics.dto;

import com.spotify.analytics.model.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class EventIngestRequest {

    @NotBlank(message = "eventId is required")
    private String eventId;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "songId is required")
    private String songId;

    @NotBlank(message = "artistId is required")
    private String artistId;

    @NotNull(message = "eventType is required")
    private EventType eventType;

    @NotNull(message = "eventTime is required")
    private Instant eventTime;

    private Integer durationSeconds;

    private String additionalMetadata;

    public EventIngestRequest() {
    }

    public EventIngestRequest(String eventId, String userId, String songId, String artistId, 
                              EventType eventType, Instant eventTime, Integer durationSeconds, 
                              String additionalMetadata) {
        this.eventId = eventId;
        this.userId = userId;
        this.songId = songId;
        this.artistId = artistId;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.durationSeconds = durationSeconds;
        this.additionalMetadata = additionalMetadata;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getAdditionalMetadata() {
        return additionalMetadata;
    }

    public void setAdditionalMetadata(String additionalMetadata) {
        this.additionalMetadata = additionalMetadata;
    }
}
