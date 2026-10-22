package com.br.ats.consult.api.job.domain.usecase;

import com.br.ats.consult.api.job.domain.model.JobPosition;

public interface DeleteJobPositionUseCase {
    JobPosition execute(Long id); // Ou void, conforme a sua preferência de retorno
}