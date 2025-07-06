package org.example.repository.master;

import org.example.entity.ServiceReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceReferenceMasterRepository extends JpaRepository<ServiceReference, Long> {
}
