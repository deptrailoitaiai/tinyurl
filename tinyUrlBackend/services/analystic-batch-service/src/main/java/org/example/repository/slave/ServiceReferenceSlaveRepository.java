package org.example.repository.slave;

import org.example.entity.ServiceReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Slave repository for ServiceReference - Read operations
 */
@Repository
public interface ServiceReferenceSlaveRepository extends JpaRepository<ServiceReference, Long> {

    /**
     * Find URL ID for a daily stats record
     */
    @Query("SELECT sr.targetId FROM ServiceReference sr WHERE sr.localId = :dailyStatsId AND sr.localTable = 'url_daily_stats' AND sr.targetTable = 'urls'")
    Optional<Long> findUrlIdByDailyStatsId(@Param("dailyStatsId") Long dailyStatsId);

    /**
     * Find daily stats ID for a URL and specific stats ID
     */
    @Query("SELECT sr.localId FROM ServiceReference sr WHERE sr.targetId = :urlId AND sr.localTable = 'url_daily_stats' AND sr.targetTable = 'urls'")
    List<Long> findDailyStatsIdsByUrlId(@Param("urlId") Long urlId);

    /**
     * Check if URL has any analytics data
     */
    @Query("SELECT COUNT(sr) > 0 FROM ServiceReference sr WHERE sr.targetId = :urlId AND sr.localTable = 'url_daily_stats' AND sr.targetTable = 'urls'")
    boolean existsByUrlId(@Param("urlId") Long urlId);
}