package com.br.ats.consult.api.candidate.domain.usecase.impl;

import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.candidate.domain.usecase.FindCandidateUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class FindCandidateUseCaseImpl implements FindCandidateUseCase {

    private final CandidateGateway gateway;

    public FindCandidateUseCaseImpl(CandidateGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Candidate> findById(Long id) {
        return gateway.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Candidate> search(String name, String role, Pageable pageable) {
        return gateway.search(trimToNull(name), trimToNull(role), pageable);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
