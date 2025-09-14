package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "url_daily_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlDailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private Long clickCount = 0L;

    @Column(name = "last_processed_click_id")
    private Long lastProcessedClickId;

    @Column(name = "last_processed_at")
    private LocalDateTime lastProcessedAt;

    @PrePersist
    protected void onCreate() {
        if (lastProcessedAt == null) {
            lastProcessedAt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastProcessedAt = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
    }

    /**
     * Get URL ID from service references table
     * This will be populated through service lookup
     */
    @Transient
    private Long urlId;
}