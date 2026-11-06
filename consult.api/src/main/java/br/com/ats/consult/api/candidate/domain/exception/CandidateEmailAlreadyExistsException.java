package br.com.ats.consult.api.candidate.domain.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class CandidateEmailAlreadyExistsException extends DomainException {

    public CandidateEmailAlreadyExistsException(String email) {
        super("Já existe um candidato com o e-mail informado: " + email);
    }
}
