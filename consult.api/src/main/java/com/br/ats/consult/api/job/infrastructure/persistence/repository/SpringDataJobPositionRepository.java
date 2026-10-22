package com.br.ats.consult.api.job.infrastructure.persistence.repository;

import com.br.ats.consult.api.job.infrastructure.persistence.entity.JobPositionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataJobPositionRepository extends JpaRepository<JobPositionEntity, Long> {

    @Query("SELECT j FROM JobPositionEntity j JOIN j.technologyIds t WHERE t = :technologyId")
    Page<JobPositionEntity> findByTechnologyId(@Param("technologyId") Long technologyId, Pageable pageable);
}
