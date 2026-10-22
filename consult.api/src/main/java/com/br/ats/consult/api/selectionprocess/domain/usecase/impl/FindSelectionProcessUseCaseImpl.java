package com.br.ats.consult.api.selectionprocess.domain.usecase.impl;

import com.br.ats.consult.api.selectionprocess.domain.exception.CandidateApplicationNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import com.br.ats.consult.api.selectionprocess.domain.usecase.FindSelectionProcessUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class FindSelectionProcessUseCaseImpl implements FindSelectionProcessUseCase {

    private final SelectionProcessGateway gateway;

    public FindSelectionProcessUseCaseImpl(SelectionProcessGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SelectionProcess> findById(Long id) {
        return gateway.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SelectionProcess> findAll(Pageable pageable) {
        return gateway.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateApplication> findApplications(Long processId) {
        return gateway.findApplicationsByProcessId(processId);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateApplication findApplication(Long applicationId) {
        return gateway.findApplicationById(applicationId)
                .orElseThrow(() -> new CandidateApplicationNotFoundException(applicationId));
    }
}
