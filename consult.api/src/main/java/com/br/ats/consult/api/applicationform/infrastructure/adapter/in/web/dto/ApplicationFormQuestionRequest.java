package com.br.ats.consult.api.applicationform.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.applicationform.domain.enums.QuestionTypeEnum;
import com.br.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ApplicationFormQuestionRequest(
        @NotBlank(message = "O enunciado da pergunta é obrigatório")
        @Size(max = 500, message = "O enunciado da pergunta não pode exceder 500 caracteres")
        String prompt,
        @NotNull(message = "O tipo da pergunta é obrigatório")
        QuestionTypeEnum type,
        boolean required,
        @Size(max = 30, message = "A pergunta não pode ter mais de 30 opções")
        List<@Size(max = 255, message = "A opção não pode exceder 255 caracteres") String> options,
        Integer position,
        @Size(max = 100, message = "A chave da pergunta não pode exceder 100 caracteres")
        String questionKey,
        TechnologyCategoryEnum technologyCategory,
        Long parentTechnologyId,
        Integer maxSelections,
        Boolean systemGenerated
) {
}
