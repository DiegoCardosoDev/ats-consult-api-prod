package br.com.ats.consult.api.applicationform.domain.model;

public record FormAnswerInput(
        Long questionId,
        String answer
) {
}
