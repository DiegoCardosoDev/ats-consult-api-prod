package br.com.ats.consult.api.selectionprocess.infrastructure.persistence.repository;

import br.com.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import br.com.ats.consult.api.selectionprocess.infrastructure.persistence.entity.CandidateApplicationEntity;
import br.com.ats.consult.api.selectionprocess.infrastructure.persistence.repository.projection.ApplicationStatusCount;
import br.com.ats.consult.api.selectionprocess.infrastructure.persistence.repository.projection.ProcessApplicationStatusCount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataCandidateApplicationRepository
        extends JpaRepository<CandidateApplicationEntity, Long> {

    @EntityGraph(attributePaths = {"candidate", "answers", "comments"})
    List<CandidateApplicationEntity> findByProcessIdOrderByAppliedAtDesc(Long processId);

    @EntityGraph(attributePaths = {"candidate", "process", "answers", "comments"})
    Optional<CandidateApplicationEntity> findWithDetailsById(Long id);

    boolean existsByProcessIdAndCandidateId(Long processId, Long candidateId);

    @Query("""
            SELECT a.process.id AS processId, a.status AS status, COUNT(a) AS total
            FROM CandidateApplicationEntity a
            WHERE a.process.id IN :processIds
            GROUP BY a.process.id, a.status
            """)
    List<ProcessApplicationStatusCount> countByProcessIds(@Param("processIds") List<Long> processIds);

    @Query("""
            SELECT a.status AS status, COUNT(a) AS total
            FROM CandidateApplicationEntity a
            WHERE (:clientId IS NULL OR a.process.client.id = :clientId)
              AND (:jobPositionId IS NULL OR a.process.jobPosition.id = :jobPositionId)
              AND (:processStatus IS NULL OR a.process.status = :processStatus)
            GROUP BY a.status
            """)
    List<ApplicationStatusCount> countByReportFilter(@Param("clientId") Long clientId,
                                                     @Param("jobPositionId") Long jobPositionId,
                                                     @Param("processStatus") SelectionProcessStatusEnum processStatus);
}
