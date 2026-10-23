package com.br.ats.consult.api.applicationform.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ApplicationFormRequest(
        @NotBlank(message = "O título do formulário é obrigatório")
        @Size(max = 255, message = "O título do formulário não pode exceder 255 caracteres")
        String title,
        @Size(max = 5000, message = "A descrição do formulário não pode exceder 5000 caracteres")
        String description,
        boolean active,
        List<@Valid ApplicationFormQuestionRequest> questions
) {
}
