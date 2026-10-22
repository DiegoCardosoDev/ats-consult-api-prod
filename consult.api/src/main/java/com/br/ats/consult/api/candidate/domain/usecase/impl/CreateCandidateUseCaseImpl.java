package com.br.ats.consult.api.candidate.domain.usecase.impl;

import com.br.ats.consult.api.candidate.domain.exception.CandidateEmailAlreadyExistsException;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.candidate.domain.usecase.CreateCandidateUseCase;
import org.springframework.transaction.annotation.Transactional;

public class CreateCandidateUseCaseImpl implements CreateCandidateUseCase {

    private final CandidateGateway gateway;

    public CreateCandidateUseCaseImpl(CandidateGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public Candidate execute(Candidate candidate) {
        candidate.normalize();
        candidate.validate();

        if (gateway.existsByEmail(candidate.getEmail())) {
            throw new CandidateEmailAlreadyExistsException(candidate.getEmail());
        }

        return gateway.save(candidate);
    }
}
