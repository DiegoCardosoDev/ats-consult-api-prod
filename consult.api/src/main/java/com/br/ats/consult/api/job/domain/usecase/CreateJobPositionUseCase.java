package com.br.ats.consult.api.job.domain.usecase;

import com.br.ats.consult.api.job.domain.model.JobPosition;

public interface CreateJobPositionUseCase {
    JobPosition execute(JobPosition jobPosition);
}