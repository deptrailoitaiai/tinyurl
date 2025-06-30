package tinyurl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tinyurl.model.entity.Url;
import tinyurl.model.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortUrl(String shortUrl);

    List<Url> findByUser(User user);

    List<Url> findByUserOrderByCreatedAtDesc(User user);

    List<Url> findByUserId(Long userId);

    List<Url> findByExpireAtBefore(LocalDateTime currentTime);

    List<Url> findByLongUrlContaining(String partialLongUrl);

}