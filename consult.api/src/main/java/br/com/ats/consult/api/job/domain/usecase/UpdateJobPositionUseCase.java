package br.com.ats.consult.api.job.domain.usecase;

import br.com.ats.consult.api.job.domain.model.JobPosition;

public interface UpdateJobPositionUseCase {
    JobPosition execute(Long id, JobPosition jobPosition);
}