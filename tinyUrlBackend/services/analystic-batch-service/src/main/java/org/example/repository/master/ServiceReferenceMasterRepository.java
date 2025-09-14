package org.example.repository.master;

import org.example.entity.ServiceReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Master repository for ServiceReference - Write operations
 */
@Repository
public interface ServiceReferenceMasterRepository extends JpaRepository<ServiceReference, Long> {

    /**
     * Find service reference for a daily stats record pointing to a URL
     */
    @Query("SELECT sr FROM ServiceReference sr WHERE sr.localId = :dailyStatsId AND sr.localTable = 'url_daily_stats' AND sr.targetTable = 'urls'")
    Optional<ServiceReference> findByDailyStatsId(@Param("dailyStatsId") Long dailyStatsId);

    /**
     * Find all URL IDs that have daily stats references
     */
    @Query("SELECT DISTINCT sr.targetId FROM ServiceReference sr WHERE sr.localTable = 'url_daily_stats' AND sr.targetTable = 'urls'")
    List<Long> findAllUrlIds();
}