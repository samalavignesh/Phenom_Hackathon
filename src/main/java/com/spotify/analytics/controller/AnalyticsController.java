package com.spotify.analytics.controller;

import com.spotify.analytics.dto.*;
import com.spotify.analytics.service.AnalyticsService;
import com.spotify.analytics.service.EventProcessingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final EventProcessingService eventProcessingService;
    private final AnalyticsService analyticsService;

    public AnalyticsController(EventProcessingService eventProcessingService, AnalyticsService analyticsService) {
        this.eventProcessingService = eventProcessingService;
        this.analyticsService = analyticsService;
    }

    @PostMapping("/events")
    public ResponseEntity<EventIngestResponse> ingestEvent(@Valid @RequestBody EventIngestRequest request) {
        EventIngestResponse response = eventProcessingService.ingestEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/trending")
    public ResponseEntity<TrendingResponse> getTrending(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "24h") String window
    ) {
        TrendingResponse response = analyticsService.getTrendingSongs(limit, window);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/songs/{songId}")
    public ResponseEntity<SongAnalyticsResponse> getSongAnalytics(@PathVariable String songId) {
        SongAnalyticsResponse response = analyticsService.getSongAnalytics(songId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overview")
    public ResponseEntity<OverviewAnalyticsResponse> getOverview() {
        OverviewAnalyticsResponse response = analyticsService.getOverview();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search-trends")
    public ResponseEntity<List<SearchTrendResponse>> getSearchTrends(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<SearchTrendResponse> response = analyticsService.getSearchTrends(limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/emerging-artists")
    public ResponseEntity<List<EmergingArtistResponse>> getEmergingArtists(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "24h") String window
    ) {
        List<EmergingArtistResponse> response = analyticsService.getEmergingArtists(limit, window);
        return ResponseEntity.ok(response);
    }
}
