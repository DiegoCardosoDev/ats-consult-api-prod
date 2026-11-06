package br.com.ats.consult.api.selectionprocess.domain.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class InvalidResumeException extends DomainException {

    public InvalidResumeException(String message) {
        super(message);
    }
}
