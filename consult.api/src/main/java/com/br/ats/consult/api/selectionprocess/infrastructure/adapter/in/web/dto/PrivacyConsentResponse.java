package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;

import java.time.Instant;

public record PrivacyConsentResponse(
        boolean accepted,
        Instant acceptedAt,
        String version
) {

    public static PrivacyConsentResponse fromDomain(CandidateApplication application) {
        if (application == null) {
            return null;
        }
        return new PrivacyConsentResponse(
                application.isConsentAccepted(),
                application.getConsentAcceptedAt(),
                application.getConsentVersion()
        );
    }
}
