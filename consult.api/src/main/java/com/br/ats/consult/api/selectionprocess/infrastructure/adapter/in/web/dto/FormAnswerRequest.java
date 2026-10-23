package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FormAnswerRequest(
        @NotNull(message = "A pergunta respondida é obrigatória")
        Long questionId,
        @Size(max = 5000, message = "A resposta não pode exceder 5000 caracteres")
        String answer
) {
}
