package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateSelectionProcessRequest(
        @NotNull(message = "O cliente é obrigatório")
        @Positive(message = "O cliente deve ser válido")
        Long clientId,
        @NotNull(message = "A vaga é obrigatória")
        @Positive(message = "A vaga deve ser válida")
        Long jobPositionId,
        SelectionProcessStatusEnum status,
        LocalDate openedAt
) {
}
