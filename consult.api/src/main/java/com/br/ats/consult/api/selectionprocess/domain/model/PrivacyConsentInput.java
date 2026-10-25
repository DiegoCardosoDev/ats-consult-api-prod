package com.br.ats.consult.api.selectionprocess.domain.model;

public record PrivacyConsentInput(
        boolean accepted,
        String version,
        String ipAddress
) {
}
