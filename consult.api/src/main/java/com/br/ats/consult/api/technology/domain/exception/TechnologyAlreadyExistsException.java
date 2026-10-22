package com.br.ats.consult.api.technology.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class TechnologyAlreadyExistsException extends DomainException {

    public TechnologyAlreadyExistsException(String name, Long parentId) {
        super(parentId == null
                ? "Já existe uma tecnologia com o nome: " + name
                : "Já existe uma tecnologia com o nome '" + name + "' para o parentId: " + parentId);
    }
}
