package com.br.ats.consult.api.applicationform.domain.model;

import com.br.ats.consult.api.applicationform.domain.enums.QuestionTypeEnum;
import com.br.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;
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
    private String questionKey;
    private TechnologyCategoryEnum technologyCategory;
    private Long parentTechnologyId;
    private Integer maxSelections;
    @Builder.Default
    private boolean systemGenerated = false;

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
        long validOptions = options == null
                ? 0
                : options.stream().filter(option -> option != null && !option.isBlank()).count();
        if ((type == QuestionTypeEnum.SINGLE_CHOICE || type == QuestionTypeEnum.MULTIPLE_CHOICE)
                && validOptions < 2) {
            throw new IllegalArgumentException("Perguntas de escolha precisam de pelo menos duas opções.");
        }
        if (maxSelections != null) {
            if (type != QuestionTypeEnum.MULTIPLE_CHOICE) {
                throw new IllegalArgumentException("O limite de seleções só se aplica a perguntas de múltipla escolha.");
            }
            if (maxSelections < 1) {
                throw new IllegalArgumentException("O limite de seleções deve ser pelo menos 1.");
            }
            if (maxSelections > validOptions) {
                throw new IllegalArgumentException("O limite de seleções não pode exceder o número de opções.");
            }
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
