package com.br.ats.consult.api.selectionprocess.domain.exception;

import com.br.ats.consult.api.shared.domain.exception.DomainException;

public class PrivacyConsentRequiredException extends DomainException {

    public PrivacyConsentRequiredException() {
        super("É obrigatório aceitar a Política de Privacidade para enviar a candidatura.");
    }

    public PrivacyConsentRequiredException(String message) {
        super(message);
    }
}
