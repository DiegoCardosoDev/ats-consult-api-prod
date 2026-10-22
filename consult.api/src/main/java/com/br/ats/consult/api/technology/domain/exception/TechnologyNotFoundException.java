package com.br.ats.consult.api.technology.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

import java.util.Collection;

public class TechnologyNotFoundException extends DomainException {
    public TechnologyNotFoundException(Long id) {
        super("Tecnologia não encontrada com o ID: " + id);
    }

    public TechnologyNotFoundException(Collection<Long> ids) {
        super("Tecnologias não encontradas com os IDs: " + ids);
    }
}