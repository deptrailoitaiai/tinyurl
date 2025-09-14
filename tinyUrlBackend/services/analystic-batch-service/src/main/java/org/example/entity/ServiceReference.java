package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "service_references")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "local_id", nullable = false)
    private Long localId;

    @Column(name = "local_table", nullable = false, length = 100)
    private String localTable;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "target_table", nullable = false, length = 100)
    private String targetTable;

    // Helper methods for URL daily stats references
    public static ServiceReference forUrlDailyStats(Long dailyStatsId, Long urlId) {
        return ServiceReference.builder()
                .localId(dailyStatsId)
                .localTable("url_daily_stats")
                .targetId(urlId)
                .targetTable("urls")
                .build();
    }

    public boolean isUrlReference() {
        return "urls".equals(targetTable);
    }

    public boolean isDailyStatsReference() {
        return "url_daily_stats".equals(localTable);
    }
}