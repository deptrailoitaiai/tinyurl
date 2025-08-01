package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "local_table", length = 100, nullable = false)
    private LocalTable localTable;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_table", length = 100, nullable = false)
    private TargetTable targetTable;

    public enum LocalTable {
        Urls("Urls");

        private final String localTable;

        LocalTable(String localTable) {
            this.localTable = localTable;
        }

        @Override
        public String toString() {
            return localTable;
        }
    }

    public enum TargetTable {
        Users("Users");

        private final String targetTable;

        TargetTable(String targetTable) {
            this.targetTable = targetTable;
        }

        @Override
        public String toString() {
            return targetTable;
        }
    }
}