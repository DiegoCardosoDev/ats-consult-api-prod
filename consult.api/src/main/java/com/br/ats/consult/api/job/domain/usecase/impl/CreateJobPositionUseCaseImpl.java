package com.br.ats.consult.api.job.domain.usecase.impl;

import com.br.ats.consult.api.job.domain.enums.JobStatusEnum;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.job.domain.usecase.CreateJobPositionUseCase;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class CreateJobPositionUseCaseImpl implements CreateJobPositionUseCase {

    private final JobPositionGateway gateway;

    private final TechnologyGateway technologyGateway;

    public CreateJobPositionUseCaseImpl(JobPositionGateway gateway, TechnologyGateway technologyGateway) {
        this.gateway = gateway;
        this.technologyGateway = technologyGateway;
    }

    @Override
    @Transactional
    public JobPosition execute(JobPosition jobPosition) {
        if (jobPosition.getStatus() == null) {
            jobPosition.setStatus(JobStatusEnum.DRAFT);
        }
        jobPosition.validate();

        // Se a vaga veio com tecnologias (apenas com IDs ou referências),
        // buscamos as tecnologias completas na base de dados para garantir a associação correta.
        if (jobPosition.getTechnologies() != null && !jobPosition.getTechnologies().isEmpty()) {
            List<Long> techIds = jobPosition.getTechnologies().stream()
                    .map(Technology::getId)
                    .toList();

            jobPosition.setTechnologies(TechnologyResolver.resolve(technologyGateway, techIds));
        }

        return gateway.save(jobPosition);
    }
}