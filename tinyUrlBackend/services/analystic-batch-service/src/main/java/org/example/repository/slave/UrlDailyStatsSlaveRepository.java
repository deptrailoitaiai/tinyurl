package org.example.repository.slave;

import org.example.entity.UrlDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Slave repository for UrlDailyStats - Read operations
 */
@Repository
public interface UrlDailyStatsSlaveRepository extends JpaRepository<UrlDailyStats, Long> {

    /**
     * Find stats for a specific URL and date (read operation)
     */
    @Query("SELECT u FROM UrlDailyStats u WHERE u.date = :date AND u.id IN " +
           "(SELECT sr.localId FROM ServiceReference sr WHERE sr.localTable = 'url_daily_stats' AND sr.targetTable = 'urls' AND sr.targetId = :urlId)")
    Optional<UrlDailyStats> findByDateAndUrlId(@Param("date") LocalDate date, @Param("urlId") Long urlId);

    /**
     * Find stats for a URL within a date range
     */
    @Query("SELECT u FROM UrlDailyStats u WHERE u.date BETWEEN :startDate AND :endDate AND u.id IN " +
           "(SELECT sr.localId FROM ServiceReference sr WHERE sr.localTable = 'url_daily_stats' AND sr.targetTable = 'urls' AND sr.targetId = :urlId) " +
           "ORDER BY u.date DESC")
    List<UrlDailyStats> findByUrlIdAndDateRange(@Param("urlId") Long urlId, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);

    /**
     * Find latest stats for a URL (last N days)
     */
    @Query("SELECT u FROM UrlDailyStats u WHERE u.id IN " +
           "(SELECT sr.localId FROM ServiceReference sr WHERE sr.localTable = 'url_daily_stats' AND sr.targetTable = 'urls' AND sr.targetId = :urlId) " +
           "ORDER BY u.date DESC")
    List<UrlDailyStats> findByUrlIdOrderByDateDesc(@Param("urlId") Long urlId);
}