package br.com.ats.consult.api.job.domain.usecase;

import br.com.ats.consult.api.job.domain.model.JobPosition;

public interface DeleteJobPositionUseCase {
    JobPosition execute(Long id); // Ou void, conforme a sua preferência de retorno
}