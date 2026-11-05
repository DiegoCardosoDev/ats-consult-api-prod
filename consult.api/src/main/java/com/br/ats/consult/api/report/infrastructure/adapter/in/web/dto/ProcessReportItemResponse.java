package com.br.ats.consult.api.report.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.report.domain.model.ProcessReportRow;
import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.ClientSummaryResponse;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.JobPositionSummaryResponse;

import java.time.LocalDate;
import java.util.Map;

public record ProcessReportItemResponse(
        Long processId,
        ClientSummaryResponse client,
        JobPositionSummaryResponse jobPosition,
        SelectionProcessStatusEnum processStatus,
        LocalDate openedAt,
        long totalApplications,
        long inTriage,
        long rejected,
        long approvedByClient,
        long hired,
        long declined,
        long standBy,
        Map<CandidateApplicationStatusEnum, Long> statusCounts
) {

    public static ProcessReportItemResponse fromDomain(ProcessReportRow row) {
        return new ProcessReportItemResponse(
                row.process().getId(),
                ClientSummaryResponse.fromDomain(row.process().getClient()),
                JobPositionSummaryResponse.fromDomain(row.process().getJobPosition()),
                row.process().getStatus(),
                row.process().getOpenedAt(),
                row.totalApplications(),
                row.count(CandidateApplicationStatusEnum.TRIAGEM),
                row.count(CandidateApplicationStatusEnum.REPROVADO),
                row.count(CandidateApplicationStatusEnum.APROVADO_CLIENTE),
                row.count(CandidateApplicationStatusEnum.CONTRATADO),
                row.count(CandidateApplicationStatusEnum.DECLINOU),
                row.count(CandidateApplicationStatusEnum.STAND_BY),
                row.statusCounts()
        );
    }
}
