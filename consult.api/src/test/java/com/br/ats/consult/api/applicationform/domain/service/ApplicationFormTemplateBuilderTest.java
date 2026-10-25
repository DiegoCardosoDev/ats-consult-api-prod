package com.br.ats.consult.api.applicationform.domain.service;

import com.br.ats.consult.api.applicationform.domain.enums.QuestionTypeEnum;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestion;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationFormTemplateBuilderTest {

    private final ApplicationFormTemplateBuilder builder = new ApplicationFormTemplateBuilder();

    @Test
    void generatesRequiredTechnologyQuestionsForEveryCategoryAndParentWithChildren() {
        Technology aws = technology(1L, "AWS", TechnologyCategoryEnum.CLOUD_PROVIDER, null);
        Technology s3 = technology(2L, "S3", TechnologyCategoryEnum.STORAGE, 1L);
        Technology java = technology(3L, "Java", TechnologyCategoryEnum.LANGUAGE, null);
        Technology postgresql = technology(4L, "PostgreSQL", TechnologyCategoryEnum.DATABASE, null);
        Technology kotlin = technology(5L, "Kotlin", TechnologyCategoryEnum.LANGUAGE, null);
        Technology react = technology(6L, "React", TechnologyCategoryEnum.FRAMEWORK, null);

        ApplicationForm form = builder.build(10L, "Vaga Backend",
                List.of(aws, java, postgresql), List.of(aws, s3, java, kotlin, postgresql, react));

        assertThat(form.getQuestions())
                .extracting(ApplicationFormQuestion::getQuestionKey)
                .contains(
                        "FULL_NAME",
                        "AGE",
                        "ROLE",
                        "EMAIL",
                        "PHONE",
                        "LINKEDIN_URL",
                        "STREET",
                        "CITY",
                        "COUNTRY",
                        "SALARY_EXPECTATION",
                        "WORK_MODEL",
                        "EDUCATION_LEVEL",
                        "ENGLISH_LEVEL",
                        "TECH_CATEGORY_LANGUAGE",
                        "TECH_CATEGORY_FRAMEWORK",
                        "TECH_CATEGORY_DATABASE",
                        "TECH_CATEGORY_CLOUD_PROVIDER",
                        "TECH_CATEGORY_STORAGE",
                        "TECH_CHILDREN_1"
                );

        assertThat(form.getQuestions())
                .filteredOn(question -> question.getQuestionKey() != null
                        && question.getQuestionKey().startsWith("TECH_"))
                .allSatisfy(question -> {
                    assertThat(question.getType()).isEqualTo(QuestionTypeEnum.MULTIPLE_CHOICE);
                    assertThat(question.isRequired()).isTrue();
                    assertThat(question.isSystemGenerated()).isTrue();
                });

        assertThat(form.getQuestions())
                .filteredOn(question -> "TECH_CATEGORY_LANGUAGE".equals(question.getQuestionKey()))
                .singleElement()
                .extracting(ApplicationFormQuestion::getOptions)
                .isEqualTo(List.of("Java", "Kotlin", "Não domino"));

        assertThat(form.getQuestions())
                .filteredOn(question -> "TECH_CHILDREN_1".equals(question.getQuestionKey()))
                .singleElement()
                .extracting(ApplicationFormQuestion::getOptions)
                .isEqualTo(List.of("S3", "Não domino"));
    }

    @Test
    void includesEveryCatalogCategoryEvenWhenJobHasNoTechnologies() {
        Technology java = technology(1L, "Java", TechnologyCategoryEnum.LANGUAGE, null);
        Technology spring = technology(2L, "Spring Boot", TechnologyCategoryEnum.FRAMEWORK, null);
        Technology aws = technology(3L, "AWS", TechnologyCategoryEnum.CLOUD_PROVIDER, null);

        ApplicationForm form = builder.build(20L, "Vaga sem tecnologias", List.of(),
                List.of(java, spring, aws));

        assertThat(form.getQuestions())
                .filteredOn(question -> question.getQuestionKey() != null
                        && question.getQuestionKey().startsWith("TECH_CATEGORY_"))
                .extracting(ApplicationFormQuestion::getQuestionKey)
                .containsExactly(
                        "TECH_CATEGORY_LANGUAGE",
                        "TECH_CATEGORY_FRAMEWORK",
                        "TECH_CATEGORY_CLOUD_PROVIDER"
                );
        assertThat(form.getQuestions())
                .filteredOn(question -> question.getQuestionKey() != null
                        && question.getQuestionKey().startsWith("TECH_CATEGORY_"))
                .allSatisfy(question -> {
                    assertThat(question.isRequired()).isTrue();
                    assertThat(question.getOptions()).contains("Não domino");
                });
    }

    private Technology technology(Long id, String name, TechnologyCategoryEnum category, Long parentId) {
        return Technology.builder()
                .id(id)
                .name(name)
                .category(category)
                .parentId(parentId)
                .build();
    }
}
