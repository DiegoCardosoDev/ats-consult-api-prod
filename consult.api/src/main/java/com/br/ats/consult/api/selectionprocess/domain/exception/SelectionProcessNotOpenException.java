package com.br.ats.consult.api.selectionprocess.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class SelectionProcessNotOpenException extends DomainException {

    public SelectionProcessNotOpenException(Long id) {
        super("O processo seletivo " + id + " não está aberto para novas candidaturas.");
    }
}
