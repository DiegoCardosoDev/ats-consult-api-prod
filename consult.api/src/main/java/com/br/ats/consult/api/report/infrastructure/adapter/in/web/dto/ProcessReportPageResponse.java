package com.br.ats.consult.api.report.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.shared.infrastructure.adapter.in.web.dto.PageResponse;

public record ProcessReportPageResponse(
        ProcessReportSummaryResponse summary,
        PageResponse<ProcessReportItemResponse> processes
) {
}
