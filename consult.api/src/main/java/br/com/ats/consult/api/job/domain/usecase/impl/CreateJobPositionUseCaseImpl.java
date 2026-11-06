package br.com.ats.consult.api.job.domain.usecase.impl;

import br.com.ats.consult.api.applicationform.domain.usecase.ManageApplicationFormUseCase;
import br.com.ats.consult.api.client.domain.exception.ClientNotFoundException;
import br.com.ats.consult.api.client.domain.port.out.ClientGateway;
import br.com.ats.consult.api.job.domain.enums.JobStatusEnum;
import br.com.ats.consult.api.job.domain.model.JobPosition;
import br.com.ats.consult.api.job.domain.port.out.JobPositionGateway;
import br.com.ats.consult.api.job.domain.usecase.CreateJobPositionUseCase;
import br.com.ats.consult.api.technology.domain.model.Technology;
import br.com.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class CreateJobPositionUseCaseImpl implements CreateJobPositionUseCase {

    private final JobPositionGateway gateway;

    private final TechnologyGateway technologyGateway;

    private final ClientGateway clientGateway;
    private final ManageApplicationFormUseCase applicationFormUseCase;

    public CreateJobPositionUseCaseImpl(JobPositionGateway gateway, TechnologyGateway technologyGateway,
                                        ClientGateway clientGateway,
                                        ManageApplicationFormUseCase applicationFormUseCase) {
        this.gateway = gateway;
        this.technologyGateway = technologyGateway;
        this.clientGateway = clientGateway;
        this.applicationFormUseCase = applicationFormUseCase;
    }

    @Override
    @Transactional
    public JobPosition execute(JobPosition jobPosition) {
        if (jobPosition.getClient() == null || jobPosition.getClient().getId() == null) {
            throw new IllegalArgumentException("O cliente da vaga é obrigatório.");
        }
        jobPosition.setClient(clientGateway.findById(jobPosition.getClient().getId())
                .orElseThrow(() -> new ClientNotFoundException(jobPosition.getClient().getId())));

        if (jobPosition.getStatus() == null) {
            jobPosition.setStatus(JobStatusEnum.DRAFT);
        }
        if (jobPosition.requiresStatusReason()) {
            if (jobPosition.getStatusReason() != null) {
                jobPosition.setStatusReason(jobPosition.getStatusReason().trim());
            }
        } else {
            jobPosition.setStatusReason(null);
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

        JobPosition saved = gateway.save(jobPosition);
        applicationFormUseCase.generateForJob(saved.getId());
        return saved;
    }
}