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

    @Column(name = "local_table", length = 100, nullable = false)
    private String localTable;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "target_table", length = 100, nullable = false)
    private String targetTable;
}