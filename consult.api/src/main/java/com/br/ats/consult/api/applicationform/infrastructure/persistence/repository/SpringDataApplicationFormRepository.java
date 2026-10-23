package com.br.ats.consult.api.applicationform.infrastructure.persistence.repository;

import com.br.ats.consult.api.applicationform.infrastructure.persistence.entity.ApplicationFormEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataApplicationFormRepository extends JpaRepository<ApplicationFormEntity, Long> {

    @EntityGraph(attributePaths = "questions")
    Optional<ApplicationFormEntity> findByJobPositionId(Long jobPositionId);

    boolean existsByJobPositionId(Long jobPositionId);

    void deleteByJobPositionId(Long jobPositionId);
}
