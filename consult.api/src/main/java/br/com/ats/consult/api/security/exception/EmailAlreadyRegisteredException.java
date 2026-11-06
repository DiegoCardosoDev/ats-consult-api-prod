package br.com.ats.consult.api.security.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class EmailAlreadyRegisteredException extends DomainException {

    public EmailAlreadyRegisteredException(String email) {
        super("O email já está registado no sistema: " + email);
    }
}
