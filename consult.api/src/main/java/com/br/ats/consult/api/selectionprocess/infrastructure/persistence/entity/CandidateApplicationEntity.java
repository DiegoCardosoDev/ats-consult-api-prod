package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity;

import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

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

    @CreationTimestamp
    @Column(name = "applied_at", nullable = false, updatable = false)
    private Instant appliedAt;
}
