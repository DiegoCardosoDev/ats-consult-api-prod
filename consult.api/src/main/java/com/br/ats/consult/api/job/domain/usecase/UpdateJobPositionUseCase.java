package com.br.ats.consult.api.job.domain.usecase;

import com.br.ats.consult.api.job.domain.model.JobPosition;

public interface UpdateJobPositionUseCase {
    JobPosition execute(Long id, JobPosition jobPosition);
}