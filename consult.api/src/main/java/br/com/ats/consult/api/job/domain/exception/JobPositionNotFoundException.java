package br.com.ats.consult.api.job.domain.exception;

import br.com.ats.consult.api.shared.domain.exception.DomainException;

public class JobPositionNotFoundException extends DomainException {
    public JobPositionNotFoundException(Long id) {
        super("Vaga não encontrada com o ID: " + id);
    }
}