package br.com.ats.consult.api.client.domain.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class ClientAlreadyExistsException extends DomainException {

    public ClientAlreadyExistsException(String message) {
        super(message);
    }
}
