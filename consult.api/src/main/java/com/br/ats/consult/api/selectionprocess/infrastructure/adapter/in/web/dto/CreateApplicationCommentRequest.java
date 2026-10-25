package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateApplicationCommentRequest(
        @NotBlank(message = "O comentário não pode estar vazio")
        @Size(max = 2000, message = "O comentário não pode exceder 2000 caracteres")
        String message
) {
}
