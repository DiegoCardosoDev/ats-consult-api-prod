package com.br.ats.consult.api.report.domain.usecase.impl;

import com.br.ats.consult.api.report.domain.model.ProcessReportFilter;
import com.br.ats.consult.api.report.domain.model.ProcessReportPage;
import com.br.ats.consult.api.report.domain.model.ProcessReportRow;
import com.br.ats.consult.api.report.domain.model.ProcessReportSummary;
import com.br.ats.consult.api.report.domain.port.out.ProcessReportGateway;
import com.br.ats.consult.api.report.domain.usecase.GenerateProcessReportUseCase;
import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public class GenerateProcessReportUseCaseImpl implements GenerateProcessReportUseCase {

    private final ProcessReportGateway gateway;

    public GenerateProcessReportUseCaseImpl(ProcessReportGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(readOnly = true)
    public ProcessReportPage execute(ProcessReportFilter filter, Pageable pageable) {
        Page<SelectionProcess> processPage = gateway.findProcesses(filter, pageable);
        List<Long> processIds = processPage.getContent().stream()
                .map(SelectionProcess::getId)
                .toList();

        Map<Long, Map<CandidateApplicationStatusEnum, Long>> countsByProcess =
                gateway.countApplicationsByProcessIds(processIds);

        List<ProcessReportRow> rows = processPage.getContent().stream()
                .map(process -> new ProcessReportRow(process,
                        countsByProcess.getOrDefault(process.getId(), Map.of())))
                .toList();

        ProcessReportSummary summary = new ProcessReportSummary(
                gateway.countProcesses(filter),
                gateway.countApplications(filter)
        );

        Page<ProcessReportRow> rowPage = new PageImpl<>(rows, pageable, processPage.getTotalElements());
        return new ProcessReportPage(summary, rowPage);
    }
}
