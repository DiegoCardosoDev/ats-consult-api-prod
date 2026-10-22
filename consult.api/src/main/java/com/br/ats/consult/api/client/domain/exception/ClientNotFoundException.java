package com.br.ats.consult.api.client.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class ClientNotFoundException extends DomainException {

    public ClientNotFoundException(Long id) {
        super("Cliente não encontrado: " + id);
    }
}
