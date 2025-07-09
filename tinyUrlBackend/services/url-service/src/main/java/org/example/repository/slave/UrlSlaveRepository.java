package org.example.repository.slave;

import org.example.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlSlaveRepository extends JpaRepository<Url, Long> {
}
