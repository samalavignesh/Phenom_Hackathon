package com.spotify.analytics.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "user_events",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tenant_event", columnNames = {"tenantId", "eventId"})
    },
    indexes = {
        @Index(name = "idx_tenant_event_time", columnList = "tenantId, eventTime"),
        @Index(name = "idx_tenant_song_time", columnList = "tenantId, songId, eventTime"),
        @Index(name = "idx_tenant_artist_time", columnList = "tenantId, artistId, eventTime")
    }
)
public class UserEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String eventId;

    @Column(nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 64)
    private String songId;

    @Column(nullable = false, length = 64)
    private String artistId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EventType eventType;

    @Column(nullable = false)
    private Instant eventTime;

    @Column(nullable = false)
    private Instant receivedAt;

    private Integer durationSeconds;

    @Column(length = 1024)
    private String additionalMetadata;

    public UserEvent() {
    }

    public UserEvent(Long id, String eventId, String tenantId, String userId, String songId, 
                     String artistId, EventType eventType, Instant eventTime, Instant receivedAt, 
                     Integer durationSeconds, String additionalMetadata) {
        this.id = id;
        this.eventId = eventId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.songId = songId;
        this.artistId = artistId;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.receivedAt = receivedAt;
        this.durationSeconds = durationSeconds;
        this.additionalMetadata = additionalMetadata;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
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
