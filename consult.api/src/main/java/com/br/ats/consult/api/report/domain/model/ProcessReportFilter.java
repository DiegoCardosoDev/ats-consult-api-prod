package com.br.ats.consult.api.report.domain.model;

import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;

public record ProcessReportFilter(
        Long clientId,
        Long jobPositionId,
        SelectionProcessStatusEnum status
) {
}
