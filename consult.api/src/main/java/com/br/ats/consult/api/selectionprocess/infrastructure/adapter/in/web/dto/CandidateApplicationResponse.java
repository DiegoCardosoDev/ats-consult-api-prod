package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto.CandidateResponse;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;

import java.time.Instant;
import java.util.List;

public record CandidateApplicationResponse(
        Long id,
        CandidateResponse candidate,
        ResumeResponse resume,
        List<FormAnswerResponse> answers,
        Instant appliedAt
) {

    public static CandidateApplicationResponse fromDomain(CandidateApplication application) {
        if (application == null) {
            return null;
        }
        return new CandidateApplicationResponse(
                application.getId(),
                CandidateResponse.fromDomain(application.getCandidate()),
                ResumeResponse.fromDomain(application.getResume()),
                application.getAnswers() == null
                        ? List.of()
                        : application.getAnswers().stream()
                        .map(FormAnswerResponse::fromDomain)
                        .toList(),
                application.getAppliedAt()
        );
    }
}
