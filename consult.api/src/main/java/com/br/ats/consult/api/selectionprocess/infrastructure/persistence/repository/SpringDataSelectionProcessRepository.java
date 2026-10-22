package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository;

import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.SelectionProcessEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataSelectionProcessRepository extends JpaRepository<SelectionProcessEntity, Long> {

    @EntityGraph(attributePaths = {"client", "jobPosition"})
    @Query("SELECT p FROM SelectionProcessEntity p")
    Page<SelectionProcessEntity> findAllWithDetails(Pageable pageable);

    @EntityGraph(attributePaths = {"client", "jobPosition"})
    @Query("SELECT p FROM SelectionProcessEntity p WHERE p.id = :id")
    Optional<SelectionProcessEntity> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"client", "jobPosition"})
    @Query("""
            SELECT p FROM SelectionProcessEntity p
            WHERE p.client.id = :clientId
              AND p.jobPosition.id = :jobPositionId
              AND p.status = :status
            """)
    Optional<SelectionProcessEntity> findByClientAndJobAndStatus(@Param("clientId") Long clientId,
                                                                 @Param("jobPositionId") Long jobPositionId,
                                                                 @Param("status") SelectionProcessStatusEnum status);
}
