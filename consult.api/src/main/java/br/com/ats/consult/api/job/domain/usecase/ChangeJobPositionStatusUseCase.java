package br.com.ats.consult.api.job.domain.usecase;

import br.com.ats.consult.api.job.domain.enums.JobStatusEnum;
import br.com.ats.consult.api.job.domain.model.JobPosition;

public interface ChangeJobPositionStatusUseCase {
    JobPosition execute(Long id, JobStatusEnum status, String reason);
}
