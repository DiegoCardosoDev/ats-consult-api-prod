package br.com.ats.consult.api.report.domain.model;

import br.com.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;

public record ProcessReportFilter(
        Long clientId,
        Long jobPositionId,
        SelectionProcessStatusEnum status
) {
}
