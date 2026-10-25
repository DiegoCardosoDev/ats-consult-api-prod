package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository;

import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.CandidateApplicationCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCandidateApplicationCommentRepository
        extends JpaRepository<CandidateApplicationCommentEntity, Long> {
}
