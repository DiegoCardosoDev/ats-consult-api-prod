package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;

import java.time.LocalDate;

public record SelectionProcessSummaryResponse(
        Long id,
        ClientSummaryResponse client,
        JobPositionSummaryResponse jobPosition,
        SelectionProcessStatusEnum status,
        LocalDate openedAt
) {

    public static SelectionProcessSummaryResponse fromDomain(SelectionProcess process) {
        return new SelectionProcessSummaryResponse(
                process.getId(),
                ClientSummaryResponse.fromDomain(process.getClient()),
                JobPositionSummaryResponse.fromDomain(process.getJobPosition()),
                process.getStatus(),
                process.getOpenedAt()
        );
    }
}
