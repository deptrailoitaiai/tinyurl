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

    /**
     * Get all users with pagination using projection
     */
    Page<UserProjection> findAllBy(Pageable pageable);

    /**
     * Find user by username for authentication (includes password hash)
     */
    Optional<UserAuthProjection> findAuthByUsername(String username);

    /**
     * Find user by email for authentication (includes password hash)
     */
    Optional<UserAuthProjection> findAuthByEmail(String email);

    /**
     * Find user by username (read-only operation)
     */
    Optional<UserProjection> findByUsername(String username);

    /**
     * Find user by email (read-only operation)
     */
    Optional<UserProjection> findByEmail(String email);

    /**
     * Find public profile by username
     */
    Optional<UserPublicProfileProjection> findPublicProfileByUsername(String username);

    /**
     * Find public profile by user ID
     */
    Optional<UserPublicProfileProjection> findPublicProfileById(Long id);

    /**
     * Find users by status with pagination (summary view)
     */
    Page<UserSummaryProjection> findSummaryByStatus(User.UserStatus status, Pageable pageable);

    /**
     * Find users by email verification status (summary view)
     */
    Page<UserSummaryProjection> findSummaryByEmailVerified(Boolean emailVerified, Pageable pageable);

    /**
     * Find users by status with pagination (full projection)
     */
    Page<UserProjection> findByStatus(User.UserStatus status, Pageable pageable);

    /**
     * Find users by email verification status (full projection)
     */
    Page<UserProjection> findByEmailVerified(Boolean emailVerified, Pageable pageable);

    /**
     * Search users by full name containing keyword
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserProjection> findByFullNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Search users by username containing keyword
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserProjection> findByUsernameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find users created within a date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Page<UserProjection> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate, 
                                               Pageable pageable);

    /**
     * Find users who logged in after a specific date
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt > :date")
    Page<UserProjection> findByLastLoginAtAfter(@Param("date") LocalDateTime date, Pageable pageable);

    /**
     * Count users by status
     */
    long countByStatus(User.UserStatus status);

    /**
     * Count verified users
     */
    long countByEmailVerified(Boolean emailVerified);

    /**
     * Find active users (status = ACTIVE and emailVerified = true)
     */
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.emailVerified = :verified")
    Page<UserProjection> findActiveVerifiedUsers(@Param("status") User.UserStatus status, 
                                                @Param("verified") Boolean verified, 
                                                Pageable pageable);

    /**
     * Find recently registered users (within last N days) - summary view
     */
    @Query("SELECT u FROM User u WHERE u.createdAt > :cutoffDate ORDER BY u.createdAt DESC")
    List<UserSummaryProjection> findRecentlyRegisteredUsersSummary(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find recently registered users (within last N days) - full view
     */
    @Query("SELECT u FROM User u WHERE u.createdAt > :cutoffDate ORDER BY u.createdAt DESC")
    List<UserProjection> findRecentlyRegisteredUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
}
