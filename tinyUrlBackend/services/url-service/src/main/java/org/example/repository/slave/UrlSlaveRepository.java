package org.example.repository.slave;

import org.example.entity.Url;
import org.example.service.data.UrlProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UrlSlaveRepository extends JpaRepository<Url, Long> {
    // Giữ lại method cũ để backward compatibility
    Page<UrlProjection> findAllBy(Pageable pageable);
    
    // Cursor pagination methods
    @Query("SELECT u FROM Url u WHERE u.id > :cursor ORDER BY u.id ASC")
    List<UrlProjection> findAllByCursorAsc(@Param("cursor") Long cursor, Pageable pageable);
    
    @Query("SELECT u FROM Url u WHERE u.id < :cursor ORDER BY u.id DESC")
    List<UrlProjection> findAllByCursorDesc(@Param("cursor") Long cursor, Pageable pageable);
    
    @Query("SELECT u FROM Url u ORDER BY u.id ASC")
    List<UrlProjection> findAllFirstPageAsc(Pageable pageable);
    
    @Query("SELECT u FROM Url u ORDER BY u.id DESC")
    List<UrlProjection> findAllFirstPageDesc(Pageable pageable);
}
