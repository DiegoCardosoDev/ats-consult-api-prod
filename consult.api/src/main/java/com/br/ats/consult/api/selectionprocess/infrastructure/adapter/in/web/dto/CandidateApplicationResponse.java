package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto.CandidateResponse;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.service.CandidateJobAdherenceCalculator;

import java.time.Instant;
import java.util.List;

public record CandidateApplicationResponse(
        Long id,
        CandidateResponse candidate,
        ResumeResponse resume,
        CandidateApplicationStatusEnum status,
        PrivacyConsentResponse consent,
        AdherenceResponse adherence,
        List<FormAnswerResponse> answers,
        List<ApplicationCommentResponse> comments,
        Instant appliedAt
) {

    public static CandidateApplicationResponse fromDomain(CandidateApplication application) {
        return fromDomain(application, null);
    }

    public static CandidateApplicationResponse fromDomain(CandidateApplication application, JobPosition jobPosition) {
        if (application == null) {
            return null;
        }
        return new CandidateApplicationResponse(
                application.getId(),
                CandidateResponse.fromDomain(application.getCandidate()),
                ResumeResponse.fromDomain(application.getResume()),
                application.getStatus(),
                PrivacyConsentResponse.fromDomain(application),
                jobPosition == null ? null
                        : AdherenceResponse.fromDomain(
                                CandidateJobAdherenceCalculator.calculate(jobPosition, application)),
                application.getAnswers() == null
                        ? List.of()
                        : application.getAnswers().stream()
                        .map(FormAnswerResponse::fromDomain)
                        .toList(),
                application.getComments() == null
                        ? List.of()
                        : application.getComments().stream()
                        .map(ApplicationCommentResponse::fromDomain)
                        .toList(),
                application.getAppliedAt()
        );
    }
}
