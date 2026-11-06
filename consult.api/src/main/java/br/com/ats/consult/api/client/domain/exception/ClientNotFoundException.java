package br.com.ats.consult.api.client.domain.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class ClientNotFoundException extends DomainException {

    public ClientNotFoundException(Long id) {
        super("Cliente não encontrado: " + id);
    }
}
