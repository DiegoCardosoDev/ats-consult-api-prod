package com.br.ats.consult.api.selectionprocess.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class CandidateApplicationNotFoundException extends DomainException {

    public CandidateApplicationNotFoundException(Long id) {
        super("Candidatura não encontrada: " + id);
    }
}
