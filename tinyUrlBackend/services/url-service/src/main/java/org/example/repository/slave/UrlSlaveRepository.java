package org.example.repository.slave;

import org.example.entity.Url;
import org.example.service.data.UrlProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlSlaveRepository extends JpaRepository<Url, Long> {
    Page<UrlProjection> findAllBy(Pageable pageable);
}
