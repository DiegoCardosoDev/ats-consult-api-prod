package br.com.ats.consult.api.selectionprocess.domain.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class SelectionProcessNotFoundException extends DomainException {

    public SelectionProcessNotFoundException(Long id) {
        super("Processo seletivo não encontrado: " + id);
    }
}
