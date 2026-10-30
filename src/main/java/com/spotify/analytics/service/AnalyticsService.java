package com.spotify.analytics.service;

import com.spotify.analytics.config.TenantContext;
import com.spotify.analytics.dto.*;
import com.spotify.analytics.exception.ResourceNotFoundException;
import com.spotify.analytics.model.EventType;
import com.spotify.analytics.model.UserEvent;
import com.spotify.analytics.repository.UserEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final UserEventRepository userEventRepository;
    private final TrendScoringService trendScoringService;

    public AnalyticsService(UserEventRepository userEventRepository, TrendScoringService trendScoringService) {
        this.userEventRepository = userEventRepository;
        this.trendScoringService = trendScoringService;
    }

    public TrendingResponse getTrendingSongs(int limit, String window) {
        String tenantId = TenantContext.getTenantId();
        Duration duration = parseWindow(window);
        Instant now = Instant.now();
        Instant currentWindowStart = now.minus(duration);
        Instant previousWindowStart = currentWindowStart.minus(duration);

        List<UserEvent> allEvents = userEventRepository.findByTenantId(tenantId);

        List<UserEvent> currentWindowEvents = allEvents.stream()
                .filter(e -> !e.getEventTime().isBefore(currentWindowStart) && !e.getEventTime().isAfter(now))
                .collect(Collectors.toList());

        List<UserEvent> previousWindowEvents = allEvents.stream()
                .filter(e -> !e.getEventTime().isBefore(previousWindowStart) && e.getEventTime().isBefore(currentWindowStart))
                .collect(Collectors.toList());

        Set<String> songIds = currentWindowEvents.stream()
                .map(UserEvent::getSongId)
                .collect(Collectors.toSet());

        if (songIds.isEmpty()) {
            return new TrendingResponse(tenantId, window, 0, Collections.emptyList());
        }

        Map<String, List<UserEvent>> currentBySong = currentWindowEvents.stream()
                .collect(Collectors.groupingBy(UserEvent::getSongId));
        Map<String, List<UserEvent>> prevBySong = previousWindowEvents.stream()
                .collect(Collectors.groupingBy(UserEvent::getSongId));

        List<SongAnalyticsResponse> ranked = new ArrayList<>();
        for (String songId : songIds) {
            List<UserEvent> currList = currentBySong.getOrDefault(songId, Collections.emptyList());
            List<UserEvent> prevList = prevBySong.getOrDefault(songId, Collections.emptyList());

            SongAnalyticsResponse res = buildSongAnalytics(songId, tenantId, currList, prevList);
            ranked.add(res);
        }

        ranked.sort((a, b) -> {
            int cmp = Double.compare(b.getTrendScore(), a.getTrendScore());
            if (cmp != 0) return cmp;
            int playCmp = Long.compare(b.getTotalPlays(), a.getTotalPlays());
            if (playCmp != 0) return playCmp;
            int listenerCmp = Long.compare(b.getUniqueListeners(), a.getUniqueListeners());
            if (listenerCmp != 0) return listenerCmp;
            return a.getSongId().compareTo(b.getSongId());
        });

        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).setRank(i + 1);
        }

        List<SongAnalyticsResponse> limited = ranked.stream()
                .limit(limit > 0 ? limit : 10)
                .collect(Collectors.toList());

        return new TrendingResponse(tenantId, window, limited.size(), limited);
    }

    public SongAnalyticsResponse getSongAnalytics(String songId) {
        String tenantId = TenantContext.getTenantId();
        List<UserEvent> songEvents = userEventRepository.findByTenantIdAndSongId(tenantId, songId);

        if (songEvents.isEmpty()) {
            throw new ResourceNotFoundException("Song not found for tenant: " + songId);
        }

        Instant now = Instant.now();
        Instant currentWindowStart = now.minus(Duration.ofHours(24));
        Instant previousWindowStart = currentWindowStart.minus(Duration.ofHours(24));

        List<UserEvent> currList = songEvents.stream()
                .filter(e -> !e.getEventTime().isBefore(currentWindowStart) && !e.getEventTime().isAfter(now))
                .collect(Collectors.toList());

        List<UserEvent> prevList = songEvents.stream()
                .filter(e -> !e.getEventTime().isBefore(previousWindowStart) && e.getEventTime().isBefore(currentWindowStart))
                .collect(Collectors.toList());

        List<UserEvent> eventsToScore = currList.isEmpty() ? songEvents : currList;
        SongAnalyticsResponse res = buildSongAnalytics(songId, tenantId, eventsToScore, prevList);
        return res;
    }

    public OverviewAnalyticsResponse getOverview() {
        String tenantId = TenantContext.getTenantId();
        List<UserEvent> allEvents = userEventRepository.findByTenantId(tenantId);

        long plays = 0;
        long likes = 0;
        long skips = 0;
        long searches = 0;
        long adds = 0;
        long totalDurationSec = 0;
        Set<String> uniqueListeners = new HashSet<>();

        for (UserEvent e : allEvents) {
            if (e.getEventType() == EventType.SONG_PLAYED || e.getEventType() == EventType.LISTENING_COMPLETED) {
                plays++;
                uniqueListeners.add(e.getUserId());
                if (e.getDurationSeconds() != null && e.getDurationSeconds() > 0) {
                    totalDurationSec += e.getDurationSeconds();
                }
            } else if (e.getEventType() == EventType.SONG_LIKED) {
                likes++;
            } else if (e.getEventType() == EventType.SONG_SKIPPED) {
                skips++;
                if (e.getDurationSeconds() != null && e.getDurationSeconds() > 0) {
                    totalDurationSec += e.getDurationSeconds();
                }
            } else if (e.getEventType() == EventType.SONG_SEARCHED) {
                searches++;
            } else if (e.getEventType() == EventType.PLAYLIST_ADDED) {
                adds++;
            }
        }

        OverviewAnalyticsResponse res = new OverviewAnalyticsResponse();
        res.setTenantId(tenantId);
        res.setTotalPlays(plays);
        res.setUniqueListeners(uniqueListeners.size());
        res.setTotalListeningHours(round(totalDurationSec / 3600.0));
        res.setTotalLikes(likes);
        res.setTotalSkips(skips);
        res.setTotalSearches(searches);
        res.setTotalPlaylistAdds(adds);
        res.setOverallLikeRate(plays > 0 ? round((double) likes / plays) : 0.0);
        res.setOverallSkipRate(plays > 0 ? round((double) skips / plays) : 0.0);
        return res;
    }

    public List<SearchTrendResponse> getSearchTrends(int limit) {
        String tenantId = TenantContext.getTenantId();
        List<Object[]> rows = userEventRepository.findTopSongsByEventType(tenantId, EventType.SONG_SEARCHED);

        List<SearchTrendResponse> results = new ArrayList<>();
        int rank = 1;
        for (Object[] r : rows) {
            String songId = (String) r[0];
            String artistId = (String) r[1];
            long count = ((Number) r[2]).longValue();
            results.add(new SearchTrendResponse(songId, artistId, count, rank++));
            if (limit > 0 && results.size() >= limit) {
                break;
            }
        }
        return results;
    }

    public List<EmergingArtistResponse> getEmergingArtists(int limit, String window) {
        String tenantId = TenantContext.getTenantId();
        Duration duration = parseWindow(window);
        Instant now = Instant.now();
        Instant currentWindowStart = now.minus(duration);
        Instant previousWindowStart = currentWindowStart.minus(duration);

        List<UserEvent> allEvents = userEventRepository.findByTenantId(tenantId);

        List<UserEvent> currentEvents = allEvents.stream()
                .filter(e -> !e.getEventTime().isBefore(currentWindowStart) && !e.getEventTime().isAfter(now))
                .collect(Collectors.toList());

        List<UserEvent> previousEvents = allEvents.stream()
                .filter(e -> !e.getEventTime().isBefore(previousWindowStart) && e.getEventTime().isBefore(currentWindowStart))
                .collect(Collectors.toList());

        Set<String> artists = allEvents.stream().map(UserEvent::getArtistId).collect(Collectors.toSet());

        Map<String, List<UserEvent>> currentByArtist = currentEvents.stream().collect(Collectors.groupingBy(UserEvent::getArtistId));
        Map<String, List<UserEvent>> prevByArtist = previousEvents.stream().collect(Collectors.groupingBy(UserEvent::getArtistId));

        List<EmergingArtistResponse> list = new ArrayList<>();

        for (String artistId : artists) {
            List<UserEvent> currList = currentByArtist.getOrDefault(artistId, Collections.emptyList());
            List<UserEvent> prevList = prevByArtist.getOrDefault(artistId, Collections.emptyList());

            long currPlays = currList.stream()
                    .filter(e -> e.getEventType() == EventType.SONG_PLAYED || e.getEventType() == EventType.LISTENING_COMPLETED)
                    .count();
            long prevPlays = prevList.stream()
                    .filter(e -> e.getEventType() == EventType.SONG_PLAYED || e.getEventType() == EventType.LISTENING_COMPLETED)
                    .count();

            long currListeners = currList.stream()
                    .filter(e -> e.getEventType() == EventType.SONG_PLAYED || e.getEventType() == EventType.LISTENING_COMPLETED)
                    .map(UserEvent::getUserId)
                    .distinct()
                    .count();
            long prevListeners = prevList.stream()
                    .filter(e -> e.getEventType() == EventType.SONG_PLAYED || e.getEventType() == EventType.LISTENING_COMPLETED)
                    .map(UserEvent::getUserId)
                    .distinct()
                    .count();

            long likes = currList.stream().filter(e -> e.getEventType() == EventType.SONG_LIKED).count();
            long adds = currList.stream().filter(e -> e.getEventType() == EventType.PLAYLIST_ADDED).count();
            long skips = currList.stream().filter(e -> e.getEventType() == EventType.SONG_SKIPPED).count();

            double playGrowth = trendScoringService.calculateGrowthPercent(currPlays, prevPlays);
            double listenerGrowth = trendScoringService.calculateGrowthPercent(currListeners, prevListeners);

            double playGrowthScore = trendScoringService.calculateGrowthScore(currPlays, prevPlays);
            double listenerGrowthScore = trendScoringService.calculateGrowthScore(currListeners, prevListeners);
            double engagementScore = trendScoringService.calculateEngagementScore(currPlays, likes, skips, adds);

            double emergingScore = round((0.40 * playGrowthScore) + (0.35 * listenerGrowthScore) + (0.25 * engagementScore));
            double engagementRate = currPlays > 0 ? round((double) (likes + adds) / currPlays) : 0.0;

            EmergingArtistResponse item = new EmergingArtistResponse();
            item.setArtistId(artistId);
            item.setCurrentPlays(currPlays);
            item.setPreviousPlays(prevPlays);
            item.setCurrentUniqueListeners(currListeners);
            item.setPreviousUniqueListeners(prevListeners);
            item.setPlayGrowthPercent(playGrowth);
            item.setListenerGrowthPercent(listenerGrowth);
            item.setEngagementRate(engagementRate);
            item.setEmergingScore(emergingScore);

            list.add(item);
        }

        list.sort((a, b) -> {
            int cmp = Double.compare(b.getEmergingScore(), a.getEmergingScore());
            if (cmp != 0) return cmp;
            int listenerCmp = Long.compare(b.getCurrentUniqueListeners(), a.getCurrentUniqueListeners());
            if (listenerCmp != 0) return listenerCmp;
            return a.getArtistId().compareTo(b.getArtistId());
        });

        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }

        return list.stream().limit(limit > 0 ? limit : 10).collect(Collectors.toList());
    }

    private SongAnalyticsResponse buildSongAnalytics(String songId, String tenantId, 
                                                    List<UserEvent> currList, List<UserEvent> prevList) {
        String artistId = "unknown";
        if (!currList.isEmpty()) {
            artistId = currList.get(0).getArtistId();
        } else if (!prevList.isEmpty()) {
            artistId = prevList.get(0).getArtistId();
        }

        long plays = 0;
        long likes = 0;
        long skips = 0;
        long searches = 0;
        long adds = 0;
        long durationSec = 0;
        Set<String> listeners = new HashSet<>();

        for (UserEvent e : currList) {
            if (e.getEventType() == EventType.SONG_PLAYED || e.getEventType() == EventType.LISTENING_COMPLETED) {
                plays++;
                listeners.add(e.getUserId());
                if (e.getDurationSeconds() != null && e.getDurationSeconds() > 0) {
                    durationSec += e.getDurationSeconds();
                }
            } else if (e.getEventType() == EventType.SONG_LIKED) {
                likes++;
            } else if (e.getEventType() == EventType.SONG_SKIPPED) {
                skips++;
                if (e.getDurationSeconds() != null && e.getDurationSeconds() > 0) {
                    durationSec += e.getDurationSeconds();
                }
            } else if (e.getEventType() == EventType.SONG_SEARCHED) {
                searches++;
            } else if (e.getEventType() == EventType.PLAYLIST_ADDED) {
                adds++;
            }
        }

        long prevPlays = 0;
        Set<String> prevListeners = new HashSet<>();
        for (UserEvent e : prevList) {
            if (e.getEventType() == EventType.SONG_PLAYED || e.getEventType() == EventType.LISTENING_COMPLETED) {
                prevPlays++;
                prevListeners.add(e.getUserId());
            }
        }

        double likeRate = plays > 0 ? round((double) likes / plays) : 0.0;
        double skipRate = plays > 0 ? round((double) skips / plays) : 0.0;
        double playGrowthPercent = trendScoringService.calculateGrowthPercent(plays, prevPlays);

        TrendScoringService.ScoreBreakdown breakdown = trendScoringService.calculateTrendScore(
                plays,
                prevPlays,
                listeners.size(),
                prevListeners.size(),
                likes,
                skips,
                adds
        );

        SongAnalyticsResponse res = new SongAnalyticsResponse();
        res.setSongId(songId);
        res.setArtistId(artistId);
        res.setTenantId(tenantId);
        res.setTotalPlays(plays);
        res.setUniqueListeners(listeners.size());
        res.setTotalListeningHours(round(durationSec / 3600.0));
        res.setTotalLikes(likes);
        res.setTotalSkips(skips);
        res.setLikeRate(likeRate);
        res.setSkipRate(skipRate);
        res.setSearchCount(searches);
        res.setPlaylistAdds(adds);
        res.setPreviousWindowPlays(prevPlays);
        res.setPreviousWindowUniqueListeners(prevListeners.size());
        res.setPlayGrowthPercent(playGrowthPercent);
        res.setPlayGrowthScore(breakdown.getPlayGrowthScore());
        res.setLikeRateScore(breakdown.getLikeRateScore());
        res.setUniqueListenerGrowthScore(breakdown.getUniqueListenerGrowthScore());
        res.setEngagementScore(breakdown.getEngagementScore());
        res.setTrendScore(breakdown.getTrendScore());
        return res;
    }

    private Duration parseWindow(String window) {
        if (window == null || window.trim().isEmpty()) {
            return Duration.ofHours(24);
        }
        String clean = window.trim().toLowerCase();
        if (clean.endsWith("h")) {
            long hours = Long.parseLong(clean.substring(0, clean.length() - 1));
            return Duration.ofHours(hours);
        } else if (clean.endsWith("d")) {
            long days = Long.parseLong(clean.substring(0, clean.length() - 1));
            return Duration.ofDays(days);
        } else if (clean.endsWith("m")) {
            long minutes = Long.parseLong(clean.substring(0, clean.length() - 1));
            return Duration.ofMinutes(minutes);
        }
        return Duration.ofHours(24);
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
