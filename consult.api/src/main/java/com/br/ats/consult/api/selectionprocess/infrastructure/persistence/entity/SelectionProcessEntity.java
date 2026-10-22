package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity;

import com.br.ats.consult.api.client.infrastructure.persistence.entity.ClientEntity;
import com.br.ats.consult.api.job.infrastructure.persistence.entity.JobPositionEntity;
import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tb_selection_processes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectionProcessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_position_id", nullable = false)
    private JobPositionEntity jobPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SelectionProcessStatusEnum status;

    @Column(name = "opened_at", nullable = false)
    private LocalDate openedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
