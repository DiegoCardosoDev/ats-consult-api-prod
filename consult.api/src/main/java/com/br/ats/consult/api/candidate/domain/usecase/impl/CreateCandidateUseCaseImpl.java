package com.br.ats.consult.api.candidate.domain.usecase.impl;

import com.br.ats.consult.api.candidate.domain.exception.CandidateEmailAlreadyExistsException;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.candidate.domain.usecase.CreateCandidateUseCase;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.transaction.annotation.Transactional;

public class CreateCandidateUseCaseImpl implements CreateCandidateUseCase {

    private final CandidateGateway gateway;
    private final TechnologyGateway technologyGateway;

    public CreateCandidateUseCaseImpl(CandidateGateway gateway, TechnologyGateway technologyGateway) {
        this.gateway = gateway;
        this.technologyGateway = technologyGateway;
    }

    @Override
    @Transactional
    public Candidate execute(Candidate candidate) {
        candidate.normalize();
        candidate.validate();
        candidate.setTechnologies(
                CandidateTechnologyResolver.resolve(technologyGateway, candidate.getTechnologies()));

        if (gateway.existsByEmail(candidate.getEmail())) {
            throw new CandidateEmailAlreadyExistsException(candidate.getEmail());
        }

        return gateway.save(candidate);
    }
}
