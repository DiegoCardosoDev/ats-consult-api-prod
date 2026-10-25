package com.br.ats.consult.api.candidate.domain.usecase.impl;

import com.br.ats.consult.api.candidate.domain.exception.CandidateEmailAlreadyExistsException;
import com.br.ats.consult.api.candidate.domain.exception.CandidateNotFoundException;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.candidate.domain.usecase.UpdateCandidateUseCase;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class UpdateCandidateUseCaseImpl implements UpdateCandidateUseCase {

    private final CandidateGateway gateway;
    private final TechnologyGateway technologyGateway;

    public UpdateCandidateUseCaseImpl(CandidateGateway gateway, TechnologyGateway technologyGateway) {
        this.gateway = gateway;
        this.technologyGateway = technologyGateway;
    }

    @Override
    @Transactional
    public Candidate execute(Long id, Candidate candidate) {
        Candidate existing = gateway.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException(id));

        candidate.setId(id);
        candidate.normalize();
        candidate.validate();
        candidate.setTechnologies(
                CandidateTechnologyResolver.resolve(technologyGateway, candidate.getTechnologies()));

        gateway.findByEmail(candidate.getEmail())
                .filter(found -> !Objects.equals(found.getId(), id))
                .ifPresent(found -> {
                    throw new CandidateEmailAlreadyExistsException(candidate.getEmail());
                });

        existing.updateFrom(candidate);
        existing.setEducationLevel(candidate.getEducationLevel());
        existing.setEnglishLevel(candidate.getEnglishLevel());
        existing.setTechnologies(candidate.getTechnologies());
        return gateway.save(existing);
    }
}
