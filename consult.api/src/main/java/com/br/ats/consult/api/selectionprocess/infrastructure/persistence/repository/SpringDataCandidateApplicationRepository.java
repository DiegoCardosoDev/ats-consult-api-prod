package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository;

import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.CandidateApplicationEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataCandidateApplicationRepository
        extends JpaRepository<CandidateApplicationEntity, Long> {

    @EntityGraph(attributePaths = {"candidate", "answers"})
    List<CandidateApplicationEntity> findByProcessIdOrderByAppliedAtDesc(Long processId);

    @EntityGraph(attributePaths = {"candidate", "process", "answers"})
    Optional<CandidateApplicationEntity> findWithDetailsById(Long id);

    boolean existsByProcessIdAndCandidateId(Long processId, Long candidateId);
}
