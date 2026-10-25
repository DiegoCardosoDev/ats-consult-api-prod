package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationStatusRequest(
        @NotNull(message = "O estado da candidatura é obrigatório")
        CandidateApplicationStatusEnum status
) {
}
