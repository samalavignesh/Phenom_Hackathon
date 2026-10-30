package com.spotify.analytics.config;

import com.spotify.analytics.dto.EventIngestRequest;
import com.spotify.analytics.model.EventType;
import com.spotify.analytics.service.EventProcessingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final EventProcessingService eventProcessingService;

    public DataInitializer(EventProcessingService eventProcessingService) {
        this.eventProcessingService = eventProcessingService;
    }

    @Override
    public void run(String... args) {
        Instant now = Instant.now();

        try {
            TenantContext.setTenantId("tenant-a");

            for (int i = 1; i <= 35; i++) {
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-stale-play-" + i,
                        "user-stale-" + (i % 3),
                        "song-stale-buzz",
                        "artist-1",
                        EventType.SONG_PLAYED,
                        now.minus(Duration.ofHours(3)).minusSeconds(i * 10),
                        45,
                        "{\"device\":\"mobile\"}"
                ));
            }
            for (int i = 1; i <= 28; i++) {
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-stale-skip-" + i,
                        "user-stale-" + (i % 3),
                        "song-stale-buzz",
                        "artist-1",
                        EventType.SONG_SKIPPED,
                        now.minus(Duration.ofHours(3)).minusSeconds(i * 10 + 2),
                        10,
                        "{\"reason\":\"bored\"}"
                ));
            }

            for (int i = 1; i <= 20; i++) {
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-breakout-play-" + i,
                        "user-new-" + i,
                        "song-breakout-hit",
                        "artist-2",
                        EventType.SONG_PLAYED,
                        now.minus(Duration.ofHours(2)).minusSeconds(i * 20),
                        210,
                        "{\"device\":\"desktop\"}"
                ));
            }
            for (int i = 1; i <= 15; i++) {
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-breakout-like-" + i,
                        "user-new-" + i,
                        "song-breakout-hit",
                        "artist-2",
                        EventType.SONG_LIKED,
                        now.minus(Duration.ofHours(2)).minusSeconds(i * 20 + 5),
                        0,
                        null
                ));
            }
            for (int i = 1; i <= 8; i++) {
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-breakout-add-" + i,
                        "user-new-" + i,
                        "song-breakout-hit",
                        "artist-2",
                        EventType.PLAYLIST_ADDED,
                        now.minus(Duration.ofHours(2)).minusSeconds(i * 20 + 8),
                        0,
                        null
                ));
            }

            for (int i = 1; i <= 10; i++) {
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-indie-search-" + i,
                        "listener-" + i,
                        "song-indie-gem",
                        "artist-3",
                        EventType.SONG_SEARCHED,
                        now.minus(Duration.ofHours(4)).minusSeconds(i * 30),
                        0,
                        null
                ));
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-indie-play-" + i,
                        "listener-" + i,
                        "song-indie-gem",
                        "artist-3",
                        EventType.SONG_PLAYED,
                        now.minus(Duration.ofHours(4)).minusSeconds(i * 30 + 10),
                        180,
                        null
                ));
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-indie-like-" + i,
                        "listener-" + i,
                        "song-indie-gem",
                        "artist-3",
                        EventType.SONG_LIKED,
                        now.minus(Duration.ofHours(4)).minusSeconds(i * 30 + 12),
                        0,
                        null
                ));
            }

            for (int i = 1; i <= 8; i++) {
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-classic-play-" + i,
                        "listener-classic-" + i,
                        "song-classic-rock",
                        "artist-1",
                        EventType.SONG_PLAYED,
                        now.minus(Duration.ofHours(5)).minusSeconds(i * 40),
                        240,
                        null
                ));
            }

            for (int i = 1; i <= 5; i++) {
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-lofi-play-" + i,
                        "listener-lofi-" + i,
                        "song-lofi-chill",
                        "artist-2",
                        EventType.SONG_PLAYED,
                        now.minus(Duration.ofHours(1)).minusSeconds(i * 50),
                        150,
                        null
                ));
            }

        } finally {
            TenantContext.clear();
        }

        try {
            TenantContext.setTenantId("tenant-b");

            for (int i = 1; i <= 12; i++) {
                eventProcessingService.ingestEvent(new EventIngestRequest(
                        "demo-tenantb-play-" + i,
                        "user-b-" + i,
                        "song-isolated-b",
                        "artist-b-exclusive",
                        EventType.SONG_PLAYED,
                        now.minus(Duration.ofHours(1)).minusSeconds(i * 15),
                        190,
                        null
                ));
            }
        } finally {
            TenantContext.clear();
        }
    }
}
