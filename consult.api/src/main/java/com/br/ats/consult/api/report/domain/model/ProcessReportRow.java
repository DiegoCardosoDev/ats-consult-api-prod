package com.br.ats.consult.api.report.domain.model;

import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;

import java.util.Map;

public record ProcessReportRow(
        SelectionProcess process,
        Map<CandidateApplicationStatusEnum, Long> statusCounts
) {

    public long totalApplications() {
        return statusCounts.values().stream().mapToLong(Long::longValue).sum();
    }

    public long count(CandidateApplicationStatusEnum status) {
        return statusCounts.getOrDefault(status, 0L);
    }
}
