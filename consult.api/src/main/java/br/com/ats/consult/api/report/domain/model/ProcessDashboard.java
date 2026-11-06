package br.com.ats.consult.api.report.domain.model;

import br.com.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import br.com.ats.consult.api.selectionprocess.domain.model.SelectionProcess;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record ProcessDashboard(
        SelectionProcess process,
        Map<CandidateApplicationStatusEnum, Long> statusCounts,
        int averageAdherence,
        long highAdherenceCount,
        List<TimelinePoint> timeline,
        List<CandidateAdherence> topCandidates
) {

    public long totalApplications() {
        return statusCounts.values().stream().mapToLong(Long::longValue).sum();
    }

    public long count(CandidateApplicationStatusEnum status) {
        return statusCounts.getOrDefault(status, 0L);
    }

    public long hired() {
        return count(CandidateApplicationStatusEnum.CONTRATADO);
    }

    public long activeApplications() {
        return totalApplications()
                - count(CandidateApplicationStatusEnum.REPROVADO)
                - count(CandidateApplicationStatusEnum.DECLINOU)
                - count(CandidateApplicationStatusEnum.CONTRATADO);
    }

    public double conversionRate() {
        long total = totalApplications();
        return total == 0 ? 0 : Math.round(hired() * 10000.0 / total) / 100.0;
    }

    public record TimelinePoint(LocalDate date, long total) {
    }

    public record CandidateAdherence(
            Long applicationId,
            Long candidateId,
            String candidateName,
            CandidateApplicationStatusEnum status,
            int adherence,
            Instant appliedAt
    ) {
    }
}
