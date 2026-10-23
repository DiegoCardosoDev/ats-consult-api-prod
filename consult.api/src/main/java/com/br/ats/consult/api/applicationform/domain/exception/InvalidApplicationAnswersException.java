package com.br.ats.consult.api.applicationform.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class InvalidApplicationAnswersException extends DomainException {

    public InvalidApplicationAnswersException(String message) {
        super(message);
    }
}
