package com.br.ats.consult.api.job.infrastructure.persistence.entity;

import com.br.ats.consult.api.job.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_job_positions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatusEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_model", nullable = false)
    private WorkModelEnum workModel;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractTypeEnum contractType;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level")
    private EducationLevelEnum educationLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "english_level")
    private EnglishLevelEnum englishLevel;

    @Column(name = "min_salary", precision = 10, scale = 2)
    private BigDecimal minSalary;

    @Column(name = "max_salary", precision = 10, scale = 2)
    private BigDecimal maxSalary;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city", column = @Column(name = "location_city")),
            @AttributeOverride(name = "state", column = @Column(name = "location_state")),
            @AttributeOverride(name = "country", column = @Column(name = "location_country"))
    })
    private LocationEmbeddable location;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "tb_job_position_technologies",
            joinColumns = @JoinColumn(name = "job_position_id")
    )
    @Column(name = "technology_id")
    @Builder.Default
    private List<Long> technologyIds = new ArrayList<>();
}