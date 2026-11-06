package br.com.ats.consult.api.selectionprocess.domain.usecase.impl;

import br.com.ats.consult.api.client.domain.exception.ClientNotFoundException;
import br.com.ats.consult.api.client.domain.port.out.ClientGateway;
import br.com.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import br.com.ats.consult.api.job.domain.port.out.JobPositionGateway;
import br.com.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import br.com.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import br.com.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import br.com.ats.consult.api.selectionprocess.domain.usecase.CreateSelectionProcessUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public class CreateSelectionProcessUseCaseImpl implements CreateSelectionProcessUseCase {

    private final SelectionProcessGateway gateway;
    private final ClientGateway clientGateway;
    private final JobPositionGateway jobPositionGateway;

    public CreateSelectionProcessUseCaseImpl(SelectionProcessGateway gateway, ClientGateway clientGateway,
                                             JobPositionGateway jobPositionGateway) {
        this.gateway = gateway;
        this.clientGateway = clientGateway;
        this.jobPositionGateway = jobPositionGateway;
    }

    @Override
    @Transactional
    public SelectionProcess execute(SelectionProcess process) {
        process.setClient(clientGateway.findById(process.getClient().getId())
                .orElseThrow(() -> new ClientNotFoundException(process.getClient().getId())));
        process.setJobPosition(jobPositionGateway.findById(process.getJobPosition().getId())
                .orElseThrow(() -> new JobPositionNotFoundException(process.getJobPosition().getId())));

        if (process.getJobPosition().getClient() == null
                || !process.getClient().getId().equals(process.getJobPosition().getClient().getId())) {
            throw new IllegalArgumentException("O cliente do processo seletivo deve ser o mesmo cliente da vaga.");
        }

        if (process.getStatus() == null) {
            process.setStatus(SelectionProcessStatusEnum.OPEN);
        }
        if (process.getOpenedAt() == null) {
            process.setOpenedAt(LocalDate.now());
        }
        process.validate();

        return gateway.save(process);
    }
}
