package com.br.ats.consult.api.selectionprocess.domain.service;

import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestionKeys;
import com.br.ats.consult.api.applicationform.domain.model.FormAnswer;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.job.domain.enums.EducationLevelEnum;
import com.br.ats.consult.api.job.domain.enums.EnglishLevelEnum;
import com.br.ats.consult.api.job.domain.enums.WorkModelEnum;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateJobAdherenceCalculatorTest {

    @Test
    void calculatesFullAdherenceWhenCandidateMatchesEveryRequirement() {
        Technology java = technology(1L, "Java");
        Technology aws = technology(2L, "AWS");
        JobPosition job = job(List.of(java, aws), WorkModelEnum.REMOTE, "5000", "8000",
                EducationLevelEnum.BACHELORS, EnglishLevelEnum.ADVANCED);
        Candidate candidate = candidate(List.of(java, aws), EducationLevelEnum.BACHELORS,
                EnglishLevelEnum.ADVANCED);
        CandidateApplication application = application(candidate, answers(
                ApplicationFormQuestionKeys.SALARY_EXPECTATION, "R$ 8.000,00",
                ApplicationFormQuestionKeys.WORK_MODEL, "REMOTE",
                ApplicationFormQuestionKeys.EDUCATION_LEVEL, "BACHELORS",
                ApplicationFormQuestionKeys.ENGLISH_LEVEL, "ADVANCED"
        ));

        CandidateJobAdherenceCalculator.Result result = CandidateJobAdherenceCalculator.calculate(job, application);

        assertThat(result.percentage()).isEqualTo(100);
        assertThat(result.criteria()).allSatisfy(criterion -> assertThat(criterion.percentage()).isEqualTo(100));
    }

    @Test
    void weighsTechnologySalaryWorkModelEducationAndEnglish() {
        Technology java = technology(1L, "Java");
        Technology aws = technology(2L, "AWS");
        Technology kotlin = technology(3L, "Kotlin");
        JobPosition job = job(List.of(java, aws, kotlin), WorkModelEnum.REMOTE, "5000", "8000",
                EducationLevelEnum.BACHELORS, EnglishLevelEnum.ADVANCED);
        Candidate candidate = candidate(List.of(java), EducationLevelEnum.ASSOCIATE,
                EnglishLevelEnum.INTERMEDIATE);
        CandidateApplication application = application(candidate, answers(
                ApplicationFormQuestionKeys.SALARY_EXPECTATION, "12000",
                ApplicationFormQuestionKeys.WORK_MODEL, "ON_SITE",
                ApplicationFormQuestionKeys.EDUCATION_LEVEL, "ASSOCIATE",
                ApplicationFormQuestionKeys.ENGLISH_LEVEL, "INTERMEDIATE"
        ));

        CandidateJobAdherenceCalculator.Result result = CandidateJobAdherenceCalculator.calculate(job, application);
        Map<String, CandidateJobAdherenceCalculator.Criterion> criteria = result.criteria().stream()
                .collect(Collectors.toMap(CandidateJobAdherenceCalculator.Criterion::key, Function.identity()));

        assertThat(result.percentage()).isEqualTo(37);
        assertThat(criteria.get("TECHNOLOGIES").percentage()).isEqualTo(33);
        assertThat(criteria.get("SALARY").percentage()).isEqualTo(50);
        assertThat(criteria.get("WORK_MODEL").percentage()).isZero();
        assertThat(criteria.get("EDUCATION").percentage()).isEqualTo(65);
        assertThat(criteria.get("ENGLISH").percentage()).isEqualTo(40);
    }

    @Test
    void ignoresCriteriaWithoutJobRequirementAndRenormalizesWeights() {
        JobPosition job = job(List.of(), WorkModelEnum.HYBRID, null, null, null, EnglishLevelEnum.NONE);
        Candidate candidate = candidate(List.of(), null, null);
        CandidateApplication application = application(candidate, answers(
                ApplicationFormQuestionKeys.WORK_MODEL, "REMOTE; HYBRID"
        ));

        CandidateJobAdherenceCalculator.Result result = CandidateJobAdherenceCalculator.calculate(job, application);

        assertThat(result.percentage()).isEqualTo(100);
        assertThat(result.criteria())
                .extracting(CandidateJobAdherenceCalculator.Criterion::key)
                .containsExactly("WORK_MODEL");
    }

    private CandidateApplication application(Candidate candidate, List<FormAnswer> answers) {
        return CandidateApplication.builder()
                .candidate(candidate)
                .answers(answers)
                .build();
    }

    private List<FormAnswer> answers(String... pairs) {
        List<FormAnswer> answers = new java.util.ArrayList<>();
        for (int index = 0; index < pairs.length; index += 2) {
            answers.add(new FormAnswer((long) index, pairs[index], pairs[index], pairs[index + 1], index));
        }
        return answers;
    }

    private Candidate candidate(List<Technology> technologies, EducationLevelEnum education,
                                EnglishLevelEnum english) {
        return Candidate.builder()
                .technologies(technologies)
                .educationLevel(education)
                .englishLevel(english)
                .build();
    }

    private JobPosition job(List<Technology> technologies, WorkModelEnum workModel, String minSalary,
                            String maxSalary, EducationLevelEnum education, EnglishLevelEnum english) {
        return JobPosition.builder()
                .technologies(technologies)
                .workModel(workModel)
                .minSalary(minSalary == null ? null : new BigDecimal(minSalary))
                .maxSalary(maxSalary == null ? null : new BigDecimal(maxSalary))
                .educationLevel(education)
                .englishLevel(english)
                .build();
    }

    private Technology technology(Long id, String name) {
        return Technology.builder()
                .id(id)
                .name(name)
                .category(TechnologyCategoryEnum.LANGUAGE)
                .build();
    }
}
