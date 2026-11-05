package com.br.ats.consult.api.report.domain.usecase;

import com.br.ats.consult.api.report.domain.model.ProcessReportFilter;
import com.br.ats.consult.api.report.domain.model.ProcessReportPage;
import org.springframework.data.domain.Pageable;

public interface GenerateProcessReportUseCase {
    ProcessReportPage execute(ProcessReportFilter filter, Pageable pageable);
}
