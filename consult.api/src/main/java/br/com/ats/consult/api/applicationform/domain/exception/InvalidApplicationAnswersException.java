package br.com.ats.consult.api.applicationform.domain.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class InvalidApplicationAnswersException extends DomainException {

    public InvalidApplicationAnswersException(String message) {
        super(message);
    }
}
