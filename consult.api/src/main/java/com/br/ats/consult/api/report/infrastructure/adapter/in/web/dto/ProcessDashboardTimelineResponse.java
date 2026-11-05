package com.br.ats.consult.api.report.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.report.domain.model.ProcessDashboard.TimelinePoint;

import java.time.LocalDate;

public record ProcessDashboardTimelineResponse(
        LocalDate date,
        long total
) {

    public static ProcessDashboardTimelineResponse fromDomain(TimelinePoint point) {
        return new ProcessDashboardTimelineResponse(point.date(), point.total());
    }
}
