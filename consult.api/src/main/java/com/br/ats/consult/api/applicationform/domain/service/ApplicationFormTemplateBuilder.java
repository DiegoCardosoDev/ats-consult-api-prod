package com.br.ats.consult.api.applicationform.domain.service;

import com.br.ats.consult.api.applicationform.domain.enums.QuestionTypeEnum;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestion;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestionKeys;
import com.br.ats.consult.api.job.domain.enums.EducationLevelEnum;
import com.br.ats.consult.api.job.domain.enums.EnglishLevelEnum;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationFormTemplateBuilder {

    private static final String NO_TECHNOLOGY_OPTION = "Não domino";

    public ApplicationForm build(Long jobPositionId, String jobTitle, List<Technology> jobTechnologies,
                                 List<Technology> catalog) {
        List<ApplicationFormQuestion> questions = new ArrayList<>(buildCoreQuestions());
        questions.add(optionalTextQuestion(ApplicationFormQuestionKeys.LINKEDIN_URL, "LinkedIn", 5));
        questions.add(optionalTextQuestion(ApplicationFormQuestionKeys.STREET, "Rua", 6));
        questions.add(optionalTextQuestion(ApplicationFormQuestionKeys.ADDRESS_NUMBER, "Número", 7));
        questions.add(optionalTextQuestion(ApplicationFormQuestionKeys.COMPLEMENT, "Complemento", 8));
        questions.add(optionalTextQuestion(ApplicationFormQuestionKeys.NEIGHBORHOOD, "Bairro", 9));
        questions.add(optionalTextQuestion(ApplicationFormQuestionKeys.CITY, "Cidade", 10));
        questions.add(optionalTextQuestion(ApplicationFormQuestionKeys.STATE, "Estado", 11));
        questions.add(optionalTextQuestion(ApplicationFormQuestionKeys.ZIP_CODE, "Código postal", 12));
        questions.add(optionalTextQuestion(ApplicationFormQuestionKeys.COUNTRY, "País", 13));

        int standardPosition = questions.size();
        questions.add(numberQuestion(ApplicationFormQuestionKeys.SALARY_EXPECTATION,
                "Pretensão salarial (R$)", standardPosition++));
        questions.add(choiceQuestion(ApplicationFormQuestionKeys.WORK_MODEL,
                "Modelo de trabalho aceito (selecione até 2)", QuestionTypeEnum.MULTIPLE_CHOICE, true, 2,
                List.of("REMOTE", "HYBRID", "ON_SITE"), standardPosition++, null, null));

        questions.add(choiceQuestion(ApplicationFormQuestionKeys.EDUCATION_LEVEL,
                "Formação acadêmica", QuestionTypeEnum.SINGLE_CHOICE, true, null,
                Arrays.stream(EducationLevelEnum.values()).map(Enum::name).toList(),
                standardPosition++, null, null));
        questions.add(choiceQuestion(ApplicationFormQuestionKeys.ENGLISH_LEVEL,
                "Nível de inglês", QuestionTypeEnum.SINGLE_CHOICE, true, null,
                Arrays.stream(EnglishLevelEnum.values()).map(Enum::name).toList(),
                standardPosition++, null, null));

        List<Technology> technologies = jobTechnologies == null ? List.of() : jobTechnologies;
        List<Technology> catalogTechnologies = catalog == null ? List.of() : catalog;
        Map<Long, List<Technology>> childrenByParent = catalogTechnologies.stream()
                .filter(technology -> technology.getParentId() != null)
                .collect(Collectors.groupingBy(Technology::getParentId));

        Map<TechnologyCategoryEnum, List<Technology>> catalogByCategory = catalogTechnologies.stream()
                .filter(technology -> technology.getCategory() != null)
                .collect(Collectors.groupingBy(Technology::getCategory));

        Map<TechnologyCategoryEnum, List<Technology>> jobByCategory = technologies.stream()
                .filter(technology -> technology.getCategory() != null)
                .sorted(Comparator.comparing(Technology::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.groupingBy(Technology::getCategory, LinkedHashMap::new, Collectors.toList()));

        int position = questions.size();
        for (TechnologyCategoryEnum category : TechnologyCategoryEnum.values()) {
            List<Technology> categoryTechnologies = catalogByCategory.getOrDefault(category, List.of());
            if (categoryTechnologies.isEmpty()) {
                categoryTechnologies = jobByCategory.getOrDefault(category, List.of());
            }
            if (categoryTechnologies.isEmpty()) {
                continue;
            }
            List<String> categoryOptions = Stream.concat(
                            catalogByCategory.getOrDefault(category, List.of()).stream(),
                            jobByCategory.getOrDefault(category, List.of()).stream())
                    .map(Technology::getName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toCollection(ArrayList::new));
            categoryOptions.add(NO_TECHNOLOGY_OPTION);
            questions.add(choiceQuestion(
                    ApplicationFormQuestionKeys.TECHNOLOGY_CATEGORY_PREFIX + category.name(),
                    "Quais tecnologias de " + categoryLabel(category) + " domina?",
                    QuestionTypeEnum.MULTIPLE_CHOICE,
                    true,
                    null,
                    categoryOptions,
                    position++,
                    category,
                    null
            ));
        }

        List<Technology> parents = technologies.stream()
                .filter(technology -> technology.getParentId() == null)
                .filter(technology -> !childrenByParent.getOrDefault(technology.getId(), List.of()).isEmpty())
                .sorted(Comparator.comparing(Technology::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        for (Technology parent : parents) {
            List<Technology> children = childrenByParent.getOrDefault(parent.getId(), List.of());
            List<String> childOptions = children.stream()
                    .sorted(Comparator.comparing(Technology::getName, String.CASE_INSENSITIVE_ORDER))
                    .map(Technology::getName)
                    .collect(Collectors.toCollection(ArrayList::new));
            childOptions.add(NO_TECHNOLOGY_OPTION);
            questions.add(choiceQuestion(
                    ApplicationFormQuestionKeys.TECHNOLOGY_CHILDREN_PREFIX + parent.getId(),
                    "Quais serviços de " + parent.getName() + " domina?",
                    QuestionTypeEnum.MULTIPLE_CHOICE,
                    true,
                    null,
                    childOptions,
                    position++,
                    parent.getCategory(),
                    parent.getId()
            ));
        }

        return ApplicationForm.builder()
                .jobPositionId(jobPositionId)
                .title("Formulário de candidatura" + (jobTitle == null || jobTitle.isBlank() ? "" : " - " + jobTitle))
                .description("Formulário gerado automaticamente com perguntas padrão e perguntas sobre as "
                        + "tecnologias do catálogo, separadas por categoria. O recrutador pode ajustar as perguntas.")
                .active(true)
                .questions(questions)
                .build();
    }

    public List<ApplicationFormQuestion> buildCoreQuestions() {
        return List.of(
                textQuestion(ApplicationFormQuestionKeys.FULL_NAME, "Nome completo", 0),
                numberQuestion(ApplicationFormQuestionKeys.AGE, "Idade", 1),
                textQuestion(ApplicationFormQuestionKeys.ROLE, "Cargo pretendido", 2),
                textQuestion(ApplicationFormQuestionKeys.EMAIL, "E-mail", 3),
                textQuestion(ApplicationFormQuestionKeys.PHONE, "Telefone", 4)
        );
    }

    private ApplicationFormQuestion textQuestion(String key, String prompt, int position) {
        return ApplicationFormQuestion.builder()
                .prompt(prompt)
                .type(QuestionTypeEnum.TEXT)
                .required(true)
                .position(position)
                .questionKey(key)
                .systemGenerated(true)
                .build();
    }

    private ApplicationFormQuestion optionalTextQuestion(String key, String prompt, int position) {
        return ApplicationFormQuestion.builder()
                .prompt(prompt)
                .type(QuestionTypeEnum.TEXT)
                .required(false)
                .position(position)
                .questionKey(key)
                .systemGenerated(true)
                .build();
    }

    private ApplicationFormQuestion numberQuestion(String key, String prompt, int position) {
        return ApplicationFormQuestion.builder()
                .prompt(prompt)
                .type(QuestionTypeEnum.NUMBER)
                .required(true)
                .position(position)
                .questionKey(key)
                .systemGenerated(true)
                .build();
    }

    private ApplicationFormQuestion choiceQuestion(String key, String prompt, QuestionTypeEnum type,
                                                   boolean required, Integer maxSelections, List<String> options,
                                                   int position, TechnologyCategoryEnum category,
                                                   Long parentTechnologyId) {
        return ApplicationFormQuestion.builder()
                .prompt(prompt)
                .type(type)
                .required(required)
                .maxSelections(maxSelections)
                .options(options.stream().filter(Objects::nonNull).toList())
                .position(position)
                .questionKey(key)
                .technologyCategory(category)
                .parentTechnologyId(parentTechnologyId)
                .systemGenerated(true)
                .build();
    }

    public static String categoryLabel(TechnologyCategoryEnum category) {
        return switch (category) {
            case LANGUAGE -> "linguagens de programação";
            case FRAMEWORK -> "frameworks";
            case DATABASE -> "bancos de dados";
            case DEVOPS_INFRA -> "DevOps e infraestrutura";
            case CLOUD_PROVIDER -> "provedores de cloud";
            case AI_LLM -> "IA e LLMs";
            case TESTING_TOOL -> "ferramentas de teste";
            case ARCHITECTURE_STYLE -> "estilos de arquitetura";
            case STORAGE -> "armazenamento";
            case SERVERLESS -> "serverless";
            case VERSION_CONTROL -> "versionamento";
            case OTHER -> "outras tecnologias";
        };
    }
}
