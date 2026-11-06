package br.com.ats.consult.api.selectionprocess.domain.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class SelectionProcessNotOpenException extends DomainException {

    public SelectionProcessNotOpenException(Long id) {
        super("O processo seletivo " + id + " não está aberto para novas candidaturas.");
    }
}
