package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplicationResult;

public record ApplyCandidateResponse(
        Long selectionProcessId,
        CandidateApplicationResponse application
) {

    public static ApplyCandidateResponse fromDomain(CandidateApplicationResult result) {
        return new ApplyCandidateResponse(
                result.selectionProcess().getId(),
                CandidateApplicationResponse.fromDomain(result.application())
        );
    }
}
