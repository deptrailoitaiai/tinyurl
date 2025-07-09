package org.example.repository.master;

import org.example.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlMasterRepository extends JpaRepository<Url, Long> {

}
