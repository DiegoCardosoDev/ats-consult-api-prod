package com.br.ats.consult.api.applicationform.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationForm {

    private Long id;
    private Long jobPositionId;
    private String title;
    @Builder.Default
    private String description = "";
    @Builder.Default
    private boolean active = true;
    @Builder.Default
    private List<ApplicationFormQuestion> questions = new ArrayList<>();

    public void validate() {
        if (jobPositionId == null) {
            throw new IllegalArgumentException("A vaga do formulário é obrigatória.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("O título do formulário é obrigatório.");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("O título do formulário não pode exceder 255 caracteres.");
        }
        if (questions == null) {
            questions = new ArrayList<>();
        }
        for (int index = 0; index < questions.size(); index++) {
            ApplicationFormQuestion question = questions.get(index);
            question.setPosition(index);
            question.validate();
        }
    }

    public boolean hasQuestions() {
        return questions != null && !questions.isEmpty();
    }
}
