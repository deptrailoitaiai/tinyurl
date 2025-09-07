package org.example.repository.slave;

import org.example.entity.User;
import org.example.service.data.UserProjection;
import org.example.service.data.UserSummaryProjection;
import org.example.service.data.UserPublicProfileProjection;
import org.example.service.data.UserAuthProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSlaveRepository extends JpaRepository<User, Long> {
}
