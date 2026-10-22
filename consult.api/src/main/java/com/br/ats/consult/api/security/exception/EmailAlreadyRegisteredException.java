package com.br.ats.consult.api.security.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class EmailAlreadyRegisteredException extends DomainException {

    public EmailAlreadyRegisteredException(String email) {
        super("O email já está registado no sistema: " + email);
    }
}
