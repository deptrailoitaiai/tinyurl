package org.example.repository.master;

import org.example.entity.ServiceReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceReferenceMasterRepository extends JpaRepository<ServiceReference, Long> {

    Optional<ServiceReference> findAllByLocalIdAndLocalTableAndTargetIdAndTargetTable(
            Long localId, ServiceReference.LocalTable localTable, Long targetId, ServiceReference.TargetTable targetTable
    );

}
