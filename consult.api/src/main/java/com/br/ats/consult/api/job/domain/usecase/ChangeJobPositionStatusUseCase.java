package com.br.ats.consult.api.job.domain.usecase;

import com.br.ats.consult.api.job.domain.enums.JobStatusEnum;
import com.br.ats.consult.api.job.domain.model.JobPosition;

public interface ChangeJobPositionStatusUseCase {
    JobPosition execute(Long id, JobStatusEnum status, String reason);
}
