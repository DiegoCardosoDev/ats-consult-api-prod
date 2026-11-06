package br.com.ats.consult.api.job.infrastructure.adapter.in.web.dto;

import br.com.ats.consult.api.job.domain.enums.JobStatusEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobStatusChangeRequest(
        @NotNull(message = "O estado da vaga é obrigatório")
        JobStatusEnum status,
        @Size(max = 500, message = "O motivo da vaga não pode exceder 500 caracteres")
        String reason
) {
}
