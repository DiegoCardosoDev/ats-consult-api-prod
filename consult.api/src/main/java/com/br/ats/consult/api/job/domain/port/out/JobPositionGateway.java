package com.br.ats.consult.api.job.domain.port.out;

import com.br.ats.consult.api.job.domain.model.JobPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface JobPositionGateway {
    JobPosition save(JobPosition jobPosition);
    JobPosition update(Long id, JobPosition jobPosition);
    Page<JobPosition> findAll(Pageable pageable);
    Optional<JobPosition> findById(Long id);
    Page<JobPosition> findByTechnologyId(Long technologyId, Pageable pageable);
    void delete(Long id);
}
