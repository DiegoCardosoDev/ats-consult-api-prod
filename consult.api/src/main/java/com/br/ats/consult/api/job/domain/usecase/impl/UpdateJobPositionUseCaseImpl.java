package com.br.ats.consult.api.job.domain.usecase.impl;

import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.job.domain.usecase.UpdateJobPositionUseCase;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public class UpdateJobPositionUseCaseImpl implements UpdateJobPositionUseCase {

    private final JobPositionGateway gateway;
    private final TechnologyGateway technologyGateway;

    public UpdateJobPositionUseCaseImpl(JobPositionGateway gateway, TechnologyGateway technologyGateway) {
        this.gateway = gateway;
        this.technologyGateway = technologyGateway;
    }

    @Override
    @Transactional
    public JobPosition execute(Long id, JobPosition jobPosition) {
        JobPosition existingJob = gateway.findById(id)
                .orElseThrow(() -> new JobPositionNotFoundException(id));

        if (jobPosition.getTechnologies() != null) {
            List<Long> techIds = jobPosition.getTechnologies().stream()
                    .map(Technology::getId)
                    .toList();

            if (techIds.isEmpty()) {
                jobPosition.setTechnologies(new ArrayList<>());
            } else {
                jobPosition.setTechnologies(TechnologyResolver.resolve(technologyGateway, techIds));
            }
        } else {
            jobPosition.setTechnologies(existingJob.getTechnologies());
        }
        jobPosition.setId(id);
        jobPosition.validate();

        return gateway.update(id, jobPosition);
    }
}