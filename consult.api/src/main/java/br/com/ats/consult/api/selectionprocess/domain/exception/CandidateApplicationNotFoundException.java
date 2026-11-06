package br.com.ats.consult.api.selectionprocess.domain.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class CandidateApplicationNotFoundException extends DomainException {

    public CandidateApplicationNotFoundException(Long id) {
        super("Candidatura não encontrada: " + id);
    }
}
