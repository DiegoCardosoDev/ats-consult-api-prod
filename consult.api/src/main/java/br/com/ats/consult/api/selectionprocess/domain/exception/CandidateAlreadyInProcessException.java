package br.com.ats.consult.api.selectionprocess.domain.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class CandidateAlreadyInProcessException extends DomainException {

    public CandidateAlreadyInProcessException(String email) {
        super("O candidato com e-mail " + email + " já está associado a este processo seletivo.");
    }
}
