package com.br.ats.consult.api.job.domain.usecase.impl;

import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.job.domain.usecase.FindJobPositionUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class FindJobPositionUseCaseImpl implements FindJobPositionUseCase {

    private final JobPositionGateway gateway;

    public FindJobPositionUseCaseImpl(JobPositionGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobPosition> findAll(Pageable pageable) {
        return gateway.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JobPosition> findById(Long id) {
        return gateway.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobPosition> findByTechnologyId(Long technologyId, Pageable pageable) {
        if (technologyId == null) {
            throw new IllegalArgumentException("O ID da tecnologia não pode ser nulo.");
        }
        return gateway.findByTechnologyId(technologyId, pageable);
    }
}
