package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;

import java.time.LocalDate;
import java.util.List;

public record SelectionProcessResponse(
        Long id,
        ClientSummaryResponse client,
        JobPositionSummaryResponse jobPosition,
        SelectionProcessStatusEnum status,
        LocalDate openedAt,
        List<CandidateApplicationResponse> candidates
) {

    public static SelectionProcessResponse fromDomain(SelectionProcess process,
                                                      List<CandidateApplication> applications) {
        return new SelectionProcessResponse(
                process.getId(),
                ClientSummaryResponse.fromDomain(process.getClient()),
                JobPositionSummaryResponse.fromDomain(process.getJobPosition()),
                process.getStatus(),
                process.getOpenedAt(),
                applications == null ? List.of() : applications.stream()
                        .map(CandidateApplicationResponse::fromDomain)
                        .toList()
        );
    }
}
