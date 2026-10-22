package com.br.ats.consult.api.technology.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTechnologyRequest(
        @NotBlank(message = "O nome não pode estar em branco")
        @Size(max = 100, message = "O nome não pode exceder 100 caracteres")
        String name,

        @NotNull(message = "A categoria é obrigatória")
        TechnologyCategoryEnum category,

        @Positive(message = "O identificador da tecnologia pai deve ser positivo")
        Long parentId
) {}
