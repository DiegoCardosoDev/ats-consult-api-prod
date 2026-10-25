package com.br.ats.consult.api.applicationform.domain.model;

public record FormAnswer(
        Long questionId,
        String questionKey,
        String questionPrompt,
        String answer,
        Integer position
) {
    public boolean isEmpty() {
        return answer == null || answer.isBlank();
    }
}
