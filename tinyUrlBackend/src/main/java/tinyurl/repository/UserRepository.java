package tinyurl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tinyurl.model.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by their username
    Optional<User> findByUsername(String username);

    // Find a user by their email
    Optional<User> findByEmail(String email);

    // Check if a user exists by username
    boolean existsByUsername(String username);

    // Check if a user exists by email
    boolean existsByEmail(String email);

}