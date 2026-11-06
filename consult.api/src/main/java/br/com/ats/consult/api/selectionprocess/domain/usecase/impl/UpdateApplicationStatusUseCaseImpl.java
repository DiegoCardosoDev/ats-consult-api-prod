package br.com.ats.consult.api.selectionprocess.domain.usecase.impl;

import br.com.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import br.com.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import br.com.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import br.com.ats.consult.api.selectionprocess.domain.usecase.UpdateApplicationStatusUseCase;
import org.springframework.transaction.annotation.Transactional;

public class UpdateApplicationStatusUseCaseImpl implements UpdateApplicationStatusUseCase {

    private final SelectionProcessGateway gateway;

    public UpdateApplicationStatusUseCaseImpl(SelectionProcessGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public CandidateApplication execute(Long applicationId, CandidateApplicationStatusEnum status) {
        if (status == null) {
            throw new IllegalArgumentException("O estado da candidatura é obrigatório.");
        }
        return gateway.updateApplicationStatus(applicationId, status);
    }
}
