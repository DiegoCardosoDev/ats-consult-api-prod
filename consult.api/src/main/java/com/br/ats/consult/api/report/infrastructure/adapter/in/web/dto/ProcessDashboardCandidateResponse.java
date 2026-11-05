package com.br.ats.consult.api.report.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.report.domain.model.ProcessDashboard.CandidateAdherence;
import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;

import java.time.Instant;

public record ProcessDashboardCandidateResponse(
        Long applicationId,
        Long candidateId,
        String candidateName,
        CandidateApplicationStatusEnum status,
        int adherence,
        Instant appliedAt
) {

    public static ProcessDashboardCandidateResponse fromDomain(CandidateAdherence candidate) {
        return new ProcessDashboardCandidateResponse(
                candidate.applicationId(),
                candidate.candidateId(),
                candidate.candidateName(),
                candidate.status(),
                candidate.adherence(),
                candidate.appliedAt()
        );
    }
}
