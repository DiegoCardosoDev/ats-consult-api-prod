package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.selectionprocess.domain.service.CandidateJobAdherenceCalculator;

import java.util.List;

public record AdherenceResponse(
        Integer percentage,
        List<AdherenceCriterionResponse> criteria
) {

    public static AdherenceResponse fromDomain(CandidateJobAdherenceCalculator.Result result) {
        if (result == null) {
            return null;
        }
        return new AdherenceResponse(
                result.percentage(),
                result.criteria().stream()
                        .map(AdherenceCriterionResponse::fromDomain)
                        .toList()
        );
    }

    public record AdherenceCriterionResponse(
            String key,
            String label,
            Integer percentage,
            Integer weight,
            String detail
    ) {
        public static AdherenceCriterionResponse fromDomain(CandidateJobAdherenceCalculator.Criterion criterion) {
            return new AdherenceCriterionResponse(
                    criterion.key(),
                    criterion.label(),
                    criterion.percentage(),
                    criterion.weight(),
                    criterion.detail()
            );
        }
    }
}
