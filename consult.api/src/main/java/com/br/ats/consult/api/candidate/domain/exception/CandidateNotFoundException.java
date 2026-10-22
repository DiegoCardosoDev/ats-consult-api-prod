package com.br.ats.consult.api.candidate.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class CandidateNotFoundException extends DomainException {

    public CandidateNotFoundException(Long id) {
        super("Candidato não encontrado: " + id);
    }
}
