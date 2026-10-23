package com.br.ats.consult.api.applicationform.domain.model;

import com.br.ats.consult.api.applicationform.domain.enums.QuestionTypeEnum;
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
public class ApplicationFormQuestion {

    private Long id;
    private String prompt;
    private QuestionTypeEnum type;
    @Builder.Default
    private boolean required = false;
    @Builder.Default
    private List<String> options = new ArrayList<>();
    private Integer position;

    public void validate() {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("O enunciado da pergunta é obrigatório.");
        }
        if (prompt.length() > 500) {
            throw new IllegalArgumentException("O enunciado da pergunta não pode exceder 500 caracteres.");
        }
        if (type == null) {
            throw new IllegalArgumentException("O tipo da pergunta é obrigatório.");
        }
        if ((type == QuestionTypeEnum.SINGLE_CHOICE || type == QuestionTypeEnum.MULTIPLE_CHOICE)
                && (options == null || options.stream().filter(option -> option != null && !option.isBlank()).count() < 2)) {
            throw new IllegalArgumentException("Perguntas de escolha precisam de pelo menos duas opções.");
        }
    }

    public List<String> normalizedOptions() {
        if (options == null) {
            return List.of();
        }
        return options.stream()
                .filter(option -> option != null && !option.isBlank())
                .map(String::trim)
                .toList();
    }
}
