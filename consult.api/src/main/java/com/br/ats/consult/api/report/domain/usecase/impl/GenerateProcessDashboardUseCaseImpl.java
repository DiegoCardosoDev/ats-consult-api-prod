package com.br.ats.consult.api.report.domain.usecase.impl;

import com.br.ats.consult.api.report.domain.model.ProcessDashboard;
import com.br.ats.consult.api.report.domain.model.ProcessDashboard.CandidateAdherence;
import com.br.ats.consult.api.report.domain.model.ProcessDashboard.TimelinePoint;
import com.br.ats.consult.api.report.domain.port.out.ProcessDashboardGateway;
import com.br.ats.consult.api.report.domain.usecase.GenerateProcessDashboardUseCase;
import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.exception.SelectionProcessNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.domain.service.CandidateJobAdherenceCalculator;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class GenerateProcessDashboardUseCaseImpl implements GenerateProcessDashboardUseCase {

    private static final int TOP_CANDIDATES = 5;
    private static final int MAX_TIMELINE_DAYS = 120;
    private static final int HIGH_ADHERENCE_THRESHOLD = 75;

    private final ProcessDashboardGateway gateway;

    public GenerateProcessDashboardUseCaseImpl(ProcessDashboardGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessDashboard execute(Long processId) {
        SelectionProcess process = gateway.findProcess(processId)
                .orElseThrow(() -> new SelectionProcessNotFoundException(processId));
        List<CandidateApplication> applications = gateway.findApplications(processId);

        Map<CandidateApplicationStatusEnum, Long> statusCounts = new EnumMap<>(CandidateApplicationStatusEnum.class);
        for (CandidateApplicationStatusEnum status : CandidateApplicationStatusEnum.values()) {
            statusCounts.put(status, 0L);
        }
        applications.stream()
                .map(CandidateApplication::getStatus)
                .filter(Objects::nonNull)
                .forEach(status -> statusCounts.merge(status, 1L, Long::sum));

        List<CandidateAdherence> scored = applications.stream()
                .map(application -> new CandidateAdherence(
                        application.getId(),
                        application.getCandidate() == null ? null : application.getCandidate().getId(),
                        application.getCandidate() == null ? null : application.getCandidate().getName(),
                        application.getStatus(),
                        CandidateJobAdherenceCalculator.calculate(process.getJobPosition(), application).percentage(),
                        application.getAppliedAt()))
                .toList();

        int averageAdherence = (int) Math.round(scored.stream()
                .mapToInt(CandidateAdherence::adherence)
                .average()
                .orElse(0));
        long highAdherenceCount = scored.stream()
                .filter(candidate -> candidate.adherence() >= HIGH_ADHERENCE_THRESHOLD)
                .count();
        List<CandidateAdherence> topCandidates = scored.stream()
                .sorted(Comparator.comparingInt(CandidateAdherence::adherence).reversed()
                        .thenComparing(CandidateAdherence::appliedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(TOP_CANDIDATES)
                .toList();

        return new ProcessDashboard(
                process,
                statusCounts,
                averageAdherence,
                highAdherenceCount,
                buildTimeline(applications),
                topCandidates
        );
    }

    private List<TimelinePoint> buildTimeline(List<CandidateApplication> applications) {
        ZoneId zone = ZoneId.systemDefault();
        TreeMap<LocalDate, Long> counts = new TreeMap<>();
        applications.stream()
                .map(CandidateApplication::getAppliedAt)
                .filter(Objects::nonNull)
                .forEach(appliedAt -> counts.merge(appliedAt.atZone(zone).toLocalDate(), 1L, Long::sum));

        if (counts.isEmpty()) {
            return List.of();
        }

        LocalDate first = counts.firstKey();
        LocalDate last = counts.lastKey();
        if (ChronoUnit.DAYS.between(first, last) > MAX_TIMELINE_DAYS) {
            return counts.entrySet().stream()
                    .map(entry -> new TimelinePoint(entry.getKey(), entry.getValue()))
                    .toList();
        }

        List<TimelinePoint> points = new ArrayList<>();
        for (LocalDate date = first; !date.isAfter(last); date = date.plusDays(1)) {
            points.add(new TimelinePoint(date, counts.getOrDefault(date, 0L)));
        }
        return points;
    }
}
