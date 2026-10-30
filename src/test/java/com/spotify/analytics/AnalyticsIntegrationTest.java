package com.spotify.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.analytics.dto.EventIngestRequest;
import com.spotify.analytics.model.EventType;
import com.spotify.analytics.repository.UserEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserEventRepository userEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userEventRepository.deleteAll();
    }

    @Test
    void testValidEventIngestion() throws Exception {
        EventIngestRequest req = new EventIngestRequest(
                "evt-int-1", "user-1", "song-1", "artist-1",
                EventType.SONG_PLAYED, Instant.now(), 180, "{\"platform\":\"web\"}"
        );

        mockMvc.perform(post("/api/analytics/events")
                        .header("X-TENANT-ID", "tenant-alpha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId", is("evt-int-1")))
                .andExpect(jsonPath("$.tenantId", is("tenant-alpha")))
                .andExpect(jsonPath("$.status", is("ACCEPTED")));
    }

    @Test
    void testInvalidEventRejection() throws Exception {
        EventIngestRequest req = new EventIngestRequest(
                "", "", "song-1", "artist-1",
                null, null, null, null
        );

        mockMvc.perform(post("/api/analytics/events")
                        .header("X-TENANT-ID", "tenant-alpha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")));
    }

    @Test
    void testDuplicateEventHandling() throws Exception {
        EventIngestRequest req = new EventIngestRequest(
                "evt-dup-1", "user-1", "song-1", "artist-1",
                EventType.SONG_PLAYED, Instant.now(), 200, null
        );

        mockMvc.perform(post("/api/analytics/events")
                        .header("X-TENANT-ID", "tenant-alpha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/analytics/events")
                        .header("X-TENANT-ID", "tenant-alpha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void testTenantIsolation() throws Exception {
        EventIngestRequest reqA = new EventIngestRequest(
                "evt-iso-1", "user-1", "song-tenant-a-only", "artist-1",
                EventType.SONG_PLAYED, Instant.now(), 200, null
        );

        mockMvc.perform(post("/api/analytics/events")
                        .header("X-TENANT-ID", "tenant-A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/analytics/overview")
                        .header("X-TENANT-ID", "tenant-A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlays", is(1)))
                .andExpect(jsonPath("$.tenantId", is("tenant-A")));

        mockMvc.perform(get("/api/analytics/overview")
                        .header("X-TENANT-ID", "tenant-B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlays", is(0)))
                .andExpect(jsonPath("$.tenantId", is("tenant-B")));

        mockMvc.perform(get("/api/analytics/trending?limit=10&window=24h")
                        .header("X-TENANT-ID", "tenant-B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)));
    }

    @Test
    void testEndToEndAnalyticsFlow() throws Exception {
        Instant now = Instant.now();

        for (int i = 0; i < 5; i++) {
            EventIngestRequest play = new EventIngestRequest(
                    "play-" + i, "user-" + i, "song-hit", "artist-top",
                    EventType.SONG_PLAYED, now.minusSeconds(100 + i), 200, null
            );
            mockMvc.perform(post("/api/analytics/events")
                            .header("X-TENANT-ID", "tenant-flow")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(play)))
                    .andExpect(status().isCreated());
        }

        EventIngestRequest like = new EventIngestRequest(
                "like-1", "user-0", "song-hit", "artist-top",
                EventType.SONG_LIKED, now.minusSeconds(50), 0, null
        );
        mockMvc.perform(post("/api/analytics/events")
                        .header("X-TENANT-ID", "tenant-flow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(like)))
                .andExpect(status().isCreated());

        EventIngestRequest search = new EventIngestRequest(
                "search-1", "user-1", "song-hit", "artist-top",
                EventType.SONG_SEARCHED, now.minusSeconds(30), 0, null
        );
        mockMvc.perform(post("/api/analytics/events")
                        .header("X-TENANT-ID", "tenant-flow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(search)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/analytics/songs/song-hit")
                        .header("X-TENANT-ID", "tenant-flow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songId", is("song-hit")))
                .andExpect(jsonPath("$.totalPlays", is(5)))
                .andExpect(jsonPath("$.uniqueListeners", is(5)))
                .andExpect(jsonPath("$.totalLikes", is(1)))
                .andExpect(jsonPath("$.trendScore", greaterThan(0.0)));

        mockMvc.perform(get("/api/analytics/search-trends")
                        .header("X-TENANT-ID", "tenant-flow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].songId", is("song-hit")))
                .andExpect(jsonPath("$[0].searchCount", is(1)));

        mockMvc.perform(get("/api/analytics/emerging-artists")
                        .header("X-TENANT-ID", "tenant-flow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].artistId", is("artist-top")));
    }
}
