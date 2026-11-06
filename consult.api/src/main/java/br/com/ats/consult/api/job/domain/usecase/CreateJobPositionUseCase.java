package br.com.ats.consult.api.job.domain.usecase;

import br.com.ats.consult.api.job.domain.model.JobPosition;

public interface CreateJobPositionUseCase {
    JobPosition execute(JobPosition jobPosition);
}