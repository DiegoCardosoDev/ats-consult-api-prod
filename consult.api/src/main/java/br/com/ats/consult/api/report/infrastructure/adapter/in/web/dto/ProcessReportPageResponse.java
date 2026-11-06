package br.com.ats.consult.api.report.infrastructure.adapter.in.web.dto;

import br.com.ats.consult.api.shared.infrastructure.adapter.in.web.dto.PageResponse;

public record ProcessReportPageResponse(
        ProcessReportSummaryResponse summary,
        PageResponse<ProcessReportItemResponse> processes
) {
}
