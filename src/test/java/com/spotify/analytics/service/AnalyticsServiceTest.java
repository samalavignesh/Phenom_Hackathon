package com.spotify.analytics.service;

import com.spotify.analytics.config.TenantContext;
import com.spotify.analytics.dto.EmergingArtistResponse;
import com.spotify.analytics.dto.OverviewAnalyticsResponse;
import com.spotify.analytics.dto.SearchTrendResponse;
import com.spotify.analytics.dto.TrendingResponse;
import com.spotify.analytics.model.EventType;
import com.spotify.analytics.model.UserEvent;
import com.spotify.analytics.repository.UserEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AnalyticsServiceTest {

    private UserEventRepository userEventRepository;
    private TrendScoringService trendScoringService;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        userEventRepository = mock(UserEventRepository.class);
        trendScoringService = new TrendScoringService();
        analyticsService = new AnalyticsService(userEventRepository, trendScoringService);
        TenantContext.setTenantId("tenant-a");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testEmptyDataset() {
        when(userEventRepository.findByTenantId("tenant-a")).thenReturn(new ArrayList<>());

        TrendingResponse trending = analyticsService.getTrendingSongs(10, "24h");
        assertEquals(0, trending.getCount());
        assertTrue(trending.getTrendingSongs().isEmpty());

        OverviewAnalyticsResponse overview = analyticsService.getOverview();
        assertEquals(0, overview.getTotalPlays());
        assertEquals(0, overview.getUniqueListeners());
        assertEquals(0.0, overview.getTotalListeningHours());
    }

    @Test
    void testPlayAndListenerAndHoursCalculation() {
        Instant now = Instant.now();
        List<UserEvent> events = List.of(
                new UserEvent(1L, "e1", "tenant-a", "u1", "s1", "a1", EventType.SONG_PLAYED, now.minusSeconds(100), now, 1800, null),
                new UserEvent(2L, "e2", "tenant-a", "u2", "s1", "a1", EventType.SONG_PLAYED, now.minusSeconds(90), now, 1800, null),
                new UserEvent(3L, "e3", "tenant-a", "u1", "s1", "a1", EventType.SONG_PLAYED, now.minusSeconds(80), now, 3600, null)
        );

        when(userEventRepository.findByTenantId("tenant-a")).thenReturn(events);

        OverviewAnalyticsResponse overview = analyticsService.getOverview();
        assertEquals(3, overview.getTotalPlays());
        assertEquals(2, overview.getUniqueListeners());
        assertEquals(2.0, overview.getTotalListeningHours());
    }

    @Test
    void testLikeAndSkipRateCalculation() {
        Instant now = Instant.now();
        List<UserEvent> events = List.of(
                new UserEvent(1L, "e1", "tenant-a", "u1", "s1", "a1", EventType.SONG_PLAYED, now.minusSeconds(100), now, 200, null),
                new UserEvent(2L, "e2", "tenant-a", "u2", "s1", "a1", EventType.SONG_PLAYED, now.minusSeconds(90), now, 200, null),
                new UserEvent(3L, "e3", "tenant-a", "u1", "s1", "a1", EventType.SONG_LIKED, now.minusSeconds(85), now, 0, null),
                new UserEvent(4L, "e4", "tenant-a", "u2", "s1", "a1", EventType.SONG_SKIPPED, now.minusSeconds(80), now, 10, null)
        );

        when(userEventRepository.findByTenantId("tenant-a")).thenReturn(events);

        OverviewAnalyticsResponse overview = analyticsService.getOverview();
        assertEquals(2, overview.getTotalPlays());
        assertEquals(1, overview.getTotalLikes());
        assertEquals(1, overview.getTotalSkips());
        assertEquals(0.5, overview.getOverallLikeRate());
        assertEquals(0.5, overview.getSkipRate());
    }

    @Test
    void testRankingMultipleSongs() {
        Instant now = Instant.now();

        List<UserEvent> events = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            events.add(new UserEvent((long) (i + 1), "e" + i, "tenant-a", "u" + i, "song-viral", "art-1", 
                    EventType.SONG_PLAYED, now.minusSeconds(1000 + i), now, 200, null));
            events.add(new UserEvent((long) (i + 100), "el" + i, "tenant-a", "u" + i, "song-viral", "art-1", 
                    EventType.SONG_LIKED, now.minusSeconds(900 + i), now, 0, null));
        }

        for (int i = 0; i < 25; i++) {
            events.add(new UserEvent((long) (i + 200), "es" + i, "tenant-a", "u" + (i % 2), "song-stale", "art-2", 
                    EventType.SONG_PLAYED, now.minusSeconds(1000 + i), now, 50, null));
            events.add(new UserEvent((long) (i + 300), "esk" + i, "tenant-a", "u" + (i % 2), "song-stale", "art-2", 
                    EventType.SONG_SKIPPED, now.minusSeconds(900 + i), now, 10, null));
        }

        when(userEventRepository.findByTenantId("tenant-a")).thenReturn(events);

        TrendingResponse trending = analyticsService.getTrendingSongs(10, "24h");
        assertEquals(2, trending.getCount());

        assertEquals("song-viral", trending.getTrendingSongs().get(0).getSongId());
        assertEquals(1, trending.getTrendingSongs().get(0).getRank());
        assertTrue(trending.getTrendingSongs().get(0).getTrendScore() > trending.getTrendingSongs().get(1).getTrendScore());
    }

    @Test
    void testLateArrivingEventRecalculation() {
        Instant baseTime = Instant.now();
        Instant eventTimeThreeDaysAgo = baseTime.minus(Duration.ofDays(3));

        List<UserEvent> events = new ArrayList<>();
        events.add(new UserEvent(1L, "e1", "tenant-a", "u1", "song-hist", "art-3", 
                EventType.SONG_PLAYED, eventTimeThreeDaysAgo, baseTime, 300, null));

        when(userEventRepository.findByTenantId("tenant-a")).thenReturn(events);

        TrendingResponse trendingRecent = analyticsService.getTrendingSongs(10, "24h");
        assertEquals(0, trendingRecent.getCount());

        TrendingResponse trendingSevenDays = analyticsService.getTrendingSongs(10, "7d");
        assertEquals(1, trendingSevenDays.getCount());
        assertEquals("song-hist", trendingSevenDays.getTrendingSongs().get(0).getSongId());
    }

    @Test
    void testSearchTrends() {
        List<Object[]> queryResults = new ArrayList<>();
        queryResults.add(new Object[]{"song-alpha", "artist-alpha", 45L});
        queryResults.add(new Object[]{"song-beta", "artist-beta", 30L});

        when(userEventRepository.findTopSongsByEventType("tenant-a", EventType.SONG_SEARCHED))
                .thenReturn(queryResults);

        List<SearchTrendResponse> trends = analyticsService.getSearchTrends(10);
        assertEquals(2, trends.size());
        assertEquals("song-alpha", trends.get(0).getSongId());
        assertEquals(45L, trends.get(0).getSearchCount());
        assertEquals(1, trends.get(0).getRank());
    }

    @Test
    void testEmergingArtists() {
        Instant now = Instant.now();
        List<UserEvent> events = List.of(
                new UserEvent(1L, "e1", "tenant-a", "u1", "s1", "rising-star", EventType.SONG_PLAYED, now.minusSeconds(100), now, 200, null),
                new UserEvent(2L, "e2", "tenant-a", "u2", "s2", "rising-star", EventType.SONG_PLAYED, now.minusSeconds(90), now, 200, null),
                new UserEvent(3L, "e3", "tenant-a", "u1", "s1", "rising-star", EventType.SONG_LIKED, now.minusSeconds(80), now, 0, null)
        );

        when(userEventRepository.findByTenantId("tenant-a")).thenReturn(events);

        List<EmergingArtistResponse> emerging = analyticsService.getEmergingArtists(10, "24h");
        assertFalse(emerging.isEmpty());
        assertEquals("rising-star", emerging.get(0).getArtistId());
        assertEquals(1, emerging.get(0).getRank());
    }
}
