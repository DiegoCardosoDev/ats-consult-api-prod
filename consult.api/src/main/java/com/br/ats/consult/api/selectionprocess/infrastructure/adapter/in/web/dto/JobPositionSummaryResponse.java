package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.job.domain.model.JobPosition;

public record JobPositionSummaryResponse(
        Long id,
        String title
) {

    public static JobPositionSummaryResponse fromDomain(JobPosition jobPosition) {
        if (jobPosition == null) {
            return null;
        }
        return new JobPositionSummaryResponse(jobPosition.getId(), jobPosition.getTitle());
    }
}
