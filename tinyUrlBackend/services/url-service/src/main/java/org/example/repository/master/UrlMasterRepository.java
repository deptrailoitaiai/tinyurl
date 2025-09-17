package org.example.repository.master;

import org.example.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UrlMasterRepository extends JpaRepository<Url, Long> {

    @Query("SELECT u FROM Url u WHERE u.expiresAt < :currentTime AND u.status = 'ACTIVE'")
    List<Url> findExpiredActiveUrls(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE Url u SET u.status = 'EXPIRED', u.updatedAt = :updatedTime WHERE u.expiresAt < :currentTime AND u.status = 'ACTIVE'")
    int updateExpiredUrlsStatus(@Param("currentTime") LocalDateTime currentTime, @Param("updatedTime") LocalDateTime updatedTime);

}
