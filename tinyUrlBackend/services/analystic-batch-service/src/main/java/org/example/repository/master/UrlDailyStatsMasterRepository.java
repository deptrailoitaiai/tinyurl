package org.example.repository.master;

import org.example.entity.UrlDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Master repository for UrlDailyStats - Write operations
 */
@Repository
public interface UrlDailyStatsMasterRepository extends JpaRepository<UrlDailyStats, Long> {

    /**
     * Find existing stats for a specific URL and date
     */
    @Query("SELECT u FROM UrlDailyStats u WHERE u.date = :date AND u.id IN " +
           "(SELECT sr.localId FROM ServiceReference sr WHERE sr.localTable = 'url_daily_stats' AND sr.targetTable = 'urls' AND sr.targetId = :urlId)")
    Optional<UrlDailyStats> findByDateAndUrlId(@Param("date") LocalDate date, @Param("urlId") Long urlId);

    /**
     * Update click count and last processed click ID
     */
    @Modifying
    @Query("UPDATE UrlDailyStats u SET u.clickCount = u.clickCount + :additionalClicks, " +
           "u.lastProcessedClickId = :lastProcessedClickId, u.lastProcessedAt = CURRENT_TIMESTAMP " +
           "WHERE u.id = :statsId")
    void updateClickCountAndLastProcessed(@Param("statsId") Long statsId, 
                                         @Param("additionalClicks") Long additionalClicks,
                                         @Param("lastProcessedClickId") Long lastProcessedClickId);
}