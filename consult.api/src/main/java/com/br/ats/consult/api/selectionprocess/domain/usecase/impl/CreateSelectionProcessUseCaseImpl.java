package com.br.ats.consult.api.selectionprocess.domain.usecase.impl;

import com.br.ats.consult.api.client.domain.exception.ClientNotFoundException;
import com.br.ats.consult.api.client.domain.port.out.ClientGateway;
import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import com.br.ats.consult.api.selectionprocess.domain.usecase.CreateSelectionProcessUseCase;
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
