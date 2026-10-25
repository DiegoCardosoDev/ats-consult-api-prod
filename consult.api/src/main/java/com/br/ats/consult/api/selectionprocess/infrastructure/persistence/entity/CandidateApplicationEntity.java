package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity;

import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateEntity;
import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tb_candidate_applications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selection_process_id", nullable = false)
    private SelectionProcessEntity process;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private CandidateEntity candidate;

    @Column(name = "resume_filename", nullable = false, length = 255)
    private String resumeFilename;

    @Column(name = "resume_content_type", nullable = false, length = 100)
    private String resumeContentType;

    @Column(name = "resume_size_bytes", nullable = false)
    private long resumeSizeBytes;

    @Column(name = "resume_storage_key", nullable = false, length = 255)
    private String resumeStorageKey;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 50)
    private CandidateApplicationStatusEnum status = CandidateApplicationStatusEnum.TRIAGEM;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    @Builder.Default
    private List<CandidateApplicationAnswerEntity> answers = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC, id DESC")
    @Builder.Default
    private Set<CandidateApplicationCommentEntity> comments = new LinkedHashSet<>();

    @Builder.Default
    @Column(name = "consent_accepted", nullable = false)
    private boolean consentAccepted = false;

    @Column(name = "consent_accepted_at")
    private Instant consentAcceptedAt;

    @Column(name = "consent_version", length = 50)
    private String consentVersion;

    @Column(name = "consent_ip", length = 45)
    private String consentIp;

    @CreationTimestamp
    @Column(name = "applied_at", nullable = false, updatable = false)
    private Instant appliedAt;
}
