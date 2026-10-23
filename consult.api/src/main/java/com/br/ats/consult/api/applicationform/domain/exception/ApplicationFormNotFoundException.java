package com.br.ats.consult.api.applicationform.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class ApplicationFormNotFoundException extends DomainException {

    public ApplicationFormNotFoundException(Long jobPositionId) {
        super("Formulário de candidatura não encontrado para a vaga: " + jobPositionId);
    }
}
