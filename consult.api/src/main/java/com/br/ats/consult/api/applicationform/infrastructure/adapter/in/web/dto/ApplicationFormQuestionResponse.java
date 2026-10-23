package com.br.ats.consult.api.applicationform.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.applicationform.domain.enums.QuestionTypeEnum;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestion;

import java.util.List;

public record ApplicationFormQuestionResponse(
        Long id,
        String prompt,
        QuestionTypeEnum type,
        boolean required,
        List<String> options,
        Integer position
) {

    public static ApplicationFormQuestionResponse fromDomain(ApplicationFormQuestion question) {
        return new ApplicationFormQuestionResponse(
                question.getId(),
                question.getPrompt(),
                question.getType(),
                question.isRequired(),
                question.getOptions() == null ? List.of() : question.getOptions(),
                question.getPosition()
        );
    }
}
