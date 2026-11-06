package br.com.ats.consult.api.report.infrastructure.adapter.in.web.dto;

import br.com.ats.consult.api.report.domain.model.ProcessReportSummary;
import br.com.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;

import java.util.Map;

public record ProcessReportSummaryResponse(
        long totalProcesses,
        long totalApplications,
        long inTriage,
        long rejected,
        long approvedByClient,
        long hired,
        long declined,
        long standBy,
        Map<CandidateApplicationStatusEnum, Long> statusCounts
) {

    public static ProcessReportSummaryResponse fromDomain(ProcessReportSummary summary) {
        return new ProcessReportSummaryResponse(
                summary.totalProcesses(),
                summary.totalApplications(),
                summary.count(CandidateApplicationStatusEnum.TRIAGEM),
                summary.count(CandidateApplicationStatusEnum.REPROVADO),
                summary.count(CandidateApplicationStatusEnum.APROVADO_CLIENTE),
                summary.count(CandidateApplicationStatusEnum.CONTRATADO),
                summary.count(CandidateApplicationStatusEnum.DECLINOU),
                summary.count(CandidateApplicationStatusEnum.STAND_BY),
                summary.statusCounts()
        );
    }
}
