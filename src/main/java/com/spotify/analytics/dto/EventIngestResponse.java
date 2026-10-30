package com.spotify.analytics.dto;

import java.time.Instant;

public class EventIngestResponse {

    private String eventId;
    private String tenantId;
    private String status;
    private Instant receivedAt;
    private String message;

    public EventIngestResponse() {
    }

    public EventIngestResponse(String eventId, String tenantId, String status, Instant receivedAt, String message) {
        this.eventId = eventId;
        this.tenantId = tenantId;
        this.status = status;
        this.receivedAt = receivedAt;
        this.message = message;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
