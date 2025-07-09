package org.example.repository.slave;

import org.example.entity.ServiceReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceReferenceSlaveRepository extends JpaRepository<ServiceReference, Long> {
}
