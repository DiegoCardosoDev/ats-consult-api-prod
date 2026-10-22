package com.br.ats.consult.api.job.domain.usecase;

import com.br.ats.consult.api.job.domain.model.JobPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FindJobPositionUseCase {
    Page<JobPosition> findAll(Pageable pageable);
    Optional<JobPosition> findById(Long id);
    Page<JobPosition> findByTechnologyId(Long technologyId, Pageable pageable);
}
