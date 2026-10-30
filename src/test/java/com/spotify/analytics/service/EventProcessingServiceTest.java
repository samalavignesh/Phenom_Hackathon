package com.spotify.analytics.service;

import com.spotify.analytics.config.TenantContext;
import com.spotify.analytics.dto.EventIngestRequest;
import com.spotify.analytics.dto.EventIngestResponse;
import com.spotify.analytics.exception.DuplicateEventException;
import com.spotify.analytics.model.EventType;
import com.spotify.analytics.model.UserEvent;
import com.spotify.analytics.repository.UserEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EventProcessingServiceTest {

    private UserEventRepository userEventRepository;
    private EventProcessingService eventProcessingService;

    @BeforeEach
    void setUp() {
        userEventRepository = mock(UserEventRepository.class);
        eventProcessingService = new EventProcessingService(userEventRepository);
        TenantContext.setTenantId("tenant-a");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testIngestEventSuccess() {
        EventIngestRequest req = new EventIngestRequest(
                "evt-100", "usr-1", "sng-1", "art-1",
                EventType.SONG_PLAYED, Instant.now(), 200, "{\"platform\":\"android\"}"
        );

        when(userEventRepository.existsByTenantIdAndEventId("tenant-a", "evt-100")).thenReturn(false);

        EventIngestResponse resp = eventProcessingService.ingestEvent(req);

        assertNotNull(resp);
        assertEquals("evt-100", resp.getEventId());
        assertEquals("tenant-a", resp.getTenantId());
        assertEquals("ACCEPTED", resp.getStatus());

        ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);
        verify(userEventRepository).save(captor.capture());
        UserEvent saved = captor.getValue();
        assertEquals("tenant-a", saved.getTenantId());
        assertEquals("sng-1", saved.getSongId());
        assertEquals(200, saved.getDurationSeconds());
    }

    @Test
    void testIngestDuplicateEventThrowsException() {
        EventIngestRequest req = new EventIngestRequest(
                "evt-100", "usr-1", "sng-1", "art-1",
                EventType.SONG_PLAYED, Instant.now(), 200, null
        );

        when(userEventRepository.existsByTenantIdAndEventId("tenant-a", "evt-100")).thenReturn(true);

        assertThrows(DuplicateEventException.class, () -> eventProcessingService.ingestEvent(req));
        verify(userEventRepository, never()).save(any(UserEvent.class));
    }
}
