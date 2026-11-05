package com.br.ats.consult.api.report.domain.model;

import org.springframework.data.domain.Page;

public record ProcessReportPage(
        ProcessReportSummary summary,
        Page<ProcessReportRow> processes
) {
}
