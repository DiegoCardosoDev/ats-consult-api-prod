package com.br.ats.consult.api.candidate.infrastructure.persistence.entity;

import com.br.ats.consult.api.job.domain.enums.EducationLevelEnum;
import com.br.ats.consult.api.job.domain.enums.EnglishLevelEnum;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_candidates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 150)
    private String role;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", length = 50)
    private EducationLevelEnum educationLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "english_level", length = 50)
    private EnglishLevelEnum englishLevel;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "tb_candidate_technologies",
            joinColumns = @JoinColumn(name = "candidate_id")
    )
    @Column(name = "technology_id")
    @Builder.Default
    private List<Long> technologyIds = new ArrayList<>();

    @Embedded
    private CandidateAddressEmbeddable address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
