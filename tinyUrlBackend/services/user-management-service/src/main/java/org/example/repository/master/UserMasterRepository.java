package org.example.repository.master;

import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserMasterRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username for authentication
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email for authentication and email operations
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Update user's last login time
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.updatedAt = :updateTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, 
                           @Param("loginTime") LocalDateTime loginTime, 
                           @Param("updateTime") LocalDateTime updateTime);

    /**
     * Update user's email verification status
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emailVerified = :verified, u.updatedAt = :updateTime WHERE u.id = :userId")
    void updateEmailVerificationStatus(@Param("userId") Long userId, 
                                     @Param("verified") Boolean verified, 
                                     @Param("updateTime") LocalDateTime updateTime);

    /**
     * Update user's status
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = :status, u.updatedAt = :updateTime WHERE u.id = :userId")
    void updateUserStatus(@Param("userId") Long userId, 
                         @Param("status") User.UserStatus status, 
                         @Param("updateTime") LocalDateTime updateTime);

    /**
     * Update user's password
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.passwordHash = :passwordHash, u.updatedAt = :updateTime WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId, 
                       @Param("passwordHash") String passwordHash, 
                       @Param("updateTime") LocalDateTime updateTime);
}
