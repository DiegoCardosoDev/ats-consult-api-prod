package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.applicationform.domain.model.FormAnswer;

public record FormAnswerResponse(
        Long questionId,
        String questionKey,
        String questionPrompt,
        String answer,
        Integer position
) {

    public static FormAnswerResponse fromDomain(FormAnswer answer) {
        return new FormAnswerResponse(
                answer.questionId(),
                answer.questionKey(),
                answer.questionPrompt(),
                answer.answer(),
                answer.position()
        );
    }
}
