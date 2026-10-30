package com.spotify.analytics.service;

import com.spotify.analytics.config.TenantContext;
import com.spotify.analytics.dto.EventIngestRequest;
import com.spotify.analytics.dto.EventIngestResponse;
import com.spotify.analytics.exception.DuplicateEventException;
import com.spotify.analytics.exception.InvalidRequestException;
import com.spotify.analytics.model.UserEvent;
import com.spotify.analytics.repository.UserEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class EventProcessingService {

    private final UserEventRepository userEventRepository;

    public EventProcessingService(UserEventRepository userEventRepository) {
        this.userEventRepository = userEventRepository;
    }

    @Transactional
    public EventIngestResponse ingestEvent(EventIngestRequest request) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new InvalidRequestException("Tenant context is required");
        }

        if (userEventRepository.existsByTenantIdAndEventId(tenantId, request.getEventId())) {
            throw new DuplicateEventException("Event with ID '" + request.getEventId() + "' already processed for tenant '" + tenantId + "'");
        }

        Instant receivedAt = Instant.now();

        UserEvent event = new UserEvent();
        event.setEventId(request.getEventId());
        event.setTenantId(tenantId);
        event.setUserId(request.getUserId());
        event.setSongId(request.getSongId());
        event.setArtistId(request.getArtistId());
        event.setEventType(request.getEventType());
        event.setEventTime(request.getEventTime());
        event.setReceivedAt(receivedAt);
        event.setDurationSeconds(request.getDurationSeconds() != null ? request.getDurationSeconds() : 0);
        event.setAdditionalMetadata(request.getAdditionalMetadata());

        userEventRepository.save(event);

        return new EventIngestResponse(
                event.getEventId(),
                tenantId,
                "ACCEPTED",
                receivedAt,
                "Event ingested successfully"
        );
    }
}
