package br.com.ats.consult.api.report.infrastructure.adapter.in.web.dto;

import br.com.ats.consult.api.report.domain.model.ProcessDashboard;
import br.com.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import br.com.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import br.com.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.ClientSummaryResponse;
import br.com.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.JobPositionSummaryResponse;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public record ProcessDashboardResponse(
        Long processId,
        ClientSummaryResponse client,
        JobPositionSummaryResponse jobPosition,
        SelectionProcessStatusEnum status,
        LocalDate openedAt,
        long daysOpen,
        long totalApplications,
        long activeApplications,
        long inTriage,
        long approvedByClient,
        long hired,
        long rejected,
        long declined,
        long standBy,
        int averageAdherence,
        long highAdherenceCount,
        double conversionRate,
        Map<CandidateApplicationStatusEnum, Long> statusCounts,
        List<ProcessDashboardTimelineResponse> timeline,
        List<ProcessDashboardCandidateResponse> topCandidates
) {

    public static ProcessDashboardResponse fromDomain(ProcessDashboard dashboard) {
        LocalDate openedAt = dashboard.process().getOpenedAt();
        long daysOpen = openedAt == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(openedAt, LocalDate.now()));

        return new ProcessDashboardResponse(
                dashboard.process().getId(),
                ClientSummaryResponse.fromDomain(dashboard.process().getClient()),
                JobPositionSummaryResponse.fromDomain(dashboard.process().getJobPosition()),
                dashboard.process().getStatus(),
                openedAt,
                daysOpen,
                dashboard.totalApplications(),
                dashboard.activeApplications(),
                dashboard.count(CandidateApplicationStatusEnum.TRIAGEM),
                dashboard.count(CandidateApplicationStatusEnum.APROVADO_CLIENTE),
                dashboard.hired(),
                dashboard.count(CandidateApplicationStatusEnum.REPROVADO),
                dashboard.count(CandidateApplicationStatusEnum.DECLINOU),
                dashboard.count(CandidateApplicationStatusEnum.STAND_BY),
                dashboard.averageAdherence(),
                dashboard.highAdherenceCount(),
                dashboard.conversionRate(),
                dashboard.statusCounts(),
                dashboard.timeline().stream()
                        .map(ProcessDashboardTimelineResponse::fromDomain)
                        .toList(),
                dashboard.topCandidates().stream()
                        .map(ProcessDashboardCandidateResponse::fromDomain)
                        .toList()
        );
    }
}
