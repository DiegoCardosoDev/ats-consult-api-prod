package br.com.ats.consult.api.job.domain.usecase.impl;

import br.com.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import br.com.ats.consult.api.job.domain.model.JobPosition;
import br.com.ats.consult.api.job.domain.port.out.JobPositionGateway;
import br.com.ats.consult.api.job.domain.usecase.DeleteJobPositionUseCase;
import org.springframework.transaction.annotation.Transactional;

public class DeleteJobPositionUseCaseImpl implements DeleteJobPositionUseCase {

    private final JobPositionGateway gateway;

    public DeleteJobPositionUseCaseImpl(JobPositionGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public JobPosition execute(Long id) {
        JobPosition existingJob = gateway.findById(id)
                .orElseThrow(() -> new JobPositionNotFoundException(id));
        gateway.delete(id);
        return existingJob;
    }
}