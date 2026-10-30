package com.spotify.analytics.repository;

import com.spotify.analytics.model.EventType;
import com.spotify.analytics.model.UserEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {

    boolean existsByTenantIdAndEventId(String tenantId, String eventId);

    List<UserEvent> findByTenantId(String tenantId);

    List<UserEvent> findByTenantIdAndEventTimeBetween(String tenantId, Instant start, Instant end);

    List<UserEvent> findByTenantIdAndSongId(String tenantId, String songId);

    List<UserEvent> findByTenantIdAndSongIdAndEventTimeBetween(String tenantId, String songId, Instant start, Instant end);

    @Query("SELECT DISTINCT e.songId FROM UserEvent e WHERE e.tenantId = :tenantId")
    List<String> findDistinctSongIdsByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT DISTINCT e.artistId FROM UserEvent e WHERE e.tenantId = :tenantId")
    List<String> findDistinctArtistIdsByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT e.songId, e.artistId, COUNT(e) as cnt FROM UserEvent e " +
           "WHERE e.tenantId = :tenantId AND e.eventType = :eventType " +
           "GROUP BY e.songId, e.artistId ORDER BY cnt DESC")
    List<Object[]> findTopSongsByEventType(@Param("tenantId") String tenantId, @Param("eventType") EventType eventType);

    @Query("SELECT e.songId, e.artistId, COUNT(e) as cnt FROM UserEvent e " +
           "WHERE e.tenantId = :tenantId AND e.eventType = :eventType AND e.eventTime BETWEEN :start AND :end " +
           "GROUP BY e.songId, e.artistId ORDER BY cnt DESC")
    List<Object[]> findTopSongsByEventTypeAndTimeBetween(
            @Param("tenantId") String tenantId,
            @Param("eventType") EventType eventType,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    List<UserEvent> findByTenantIdAndArtistId(String tenantId, String artistId);
}
