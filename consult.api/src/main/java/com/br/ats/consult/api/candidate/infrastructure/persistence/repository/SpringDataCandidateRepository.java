package com.br.ats.consult.api.candidate.infrastructure.persistence.repository;

import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCandidateRepository extends JpaRepository<CandidateEntity, Long> {

    Optional<CandidateEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<CandidateEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<CandidateEntity> findByRoleContainingIgnoreCase(String role, Pageable pageable);

    Page<CandidateEntity> findByNameContainingIgnoreCaseAndRoleContainingIgnoreCase(
            String name, String role, Pageable pageable);
}
