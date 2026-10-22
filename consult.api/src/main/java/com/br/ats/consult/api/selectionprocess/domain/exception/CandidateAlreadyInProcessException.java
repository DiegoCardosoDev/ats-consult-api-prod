package com.br.ats.consult.api.selectionprocess.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class CandidateAlreadyInProcessException extends DomainException {

    public CandidateAlreadyInProcessException(String email) {
        super("O candidato com e-mail " + email + " já está associado a este processo seletivo.");
    }
}
