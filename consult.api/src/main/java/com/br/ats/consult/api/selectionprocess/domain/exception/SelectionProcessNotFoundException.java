package com.br.ats.consult.api.selectionprocess.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class SelectionProcessNotFoundException extends DomainException {

    public SelectionProcessNotFoundException(Long id) {
        super("Processo seletivo não encontrado: " + id);
    }
}
