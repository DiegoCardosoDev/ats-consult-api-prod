package com.br.ats.consult.api.report.infrastructure.adapter.in.web;

import com.br.ats.consult.api.report.domain.model.ProcessReportFilter;
import com.br.ats.consult.api.report.domain.model.ProcessReportPage;
import com.br.ats.consult.api.report.domain.usecase.GenerateProcessDashboardUseCase;
import com.br.ats.consult.api.report.domain.usecase.GenerateProcessReportUseCase;
import com.br.ats.consult.api.report.infrastructure.adapter.in.web.dto.ProcessDashboardResponse;
import com.br.ats.consult.api.report.infrastructure.adapter.in.web.dto.ProcessReportItemResponse;
import com.br.ats.consult.api.report.infrastructure.adapter.in.web.dto.ProcessReportPageResponse;
import com.br.ats.consult.api.report.infrastructure.adapter.in.web.dto.ProcessReportSummaryResponse;
import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import com.br.ats.consult.api.shared.infrastructure.adapter.in.web.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports/selection-processes")
public class SelectionProcessReportController {

    private final GenerateProcessReportUseCase generateProcessReportUseCase;
    private final GenerateProcessDashboardUseCase generateProcessDashboardUseCase;

    public SelectionProcessReportController(GenerateProcessReportUseCase generateProcessReportUseCase,
                                            GenerateProcessDashboardUseCase generateProcessDashboardUseCase) {
        this.generateProcessReportUseCase = generateProcessReportUseCase;
        this.generateProcessDashboardUseCase = generateProcessDashboardUseCase;
    }

    @GetMapping
    public ResponseEntity<ProcessReportPageResponse> generate(
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long jobPositionId,
            @RequestParam(required = false) SelectionProcessStatusEnum status,
            @PageableDefault(size = 20, sort = "openedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        ProcessReportPage report = generateProcessReportUseCase.execute(
                new ProcessReportFilter(clientId, jobPositionId, status), pageable);

        PageResponse<ProcessReportItemResponse> processes = PageResponse.from(
                report.processes().map(ProcessReportItemResponse::fromDomain));

        return ResponseEntity.ok(new ProcessReportPageResponse(
                ProcessReportSummaryResponse.fromDomain(report.summary()), processes));
    }

    @GetMapping("/{processId}")
    public ResponseEntity<ProcessDashboardResponse> dashboard(@PathVariable Long processId) {
        return ResponseEntity.ok(ProcessDashboardResponse.fromDomain(
                generateProcessDashboardUseCase.execute(processId)));
    }
}
