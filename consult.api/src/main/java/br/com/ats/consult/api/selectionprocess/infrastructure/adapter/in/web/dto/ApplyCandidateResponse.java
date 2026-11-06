package br.com.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import br.com.ats.consult.api.selectionprocess.domain.model.CandidateApplicationResult;

public record ApplyCandidateResponse(
        Long selectionProcessId,
        CandidateApplicationResponse application
) {

    public static ApplyCandidateResponse fromDomain(CandidateApplicationResult result) {
        return new ApplyCandidateResponse(
                result.selectionProcess().getId(),
                CandidateApplicationResponse.fromDomain(result.application(),
                        result.selectionProcess().getJobPosition())
        );
    }
}
