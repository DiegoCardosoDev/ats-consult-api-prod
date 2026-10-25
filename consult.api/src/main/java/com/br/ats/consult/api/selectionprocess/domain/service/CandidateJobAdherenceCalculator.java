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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class CandidateJobAdherenceCalculator {

    private static final int TECHNOLOGY_WEIGHT = 35;
    private static final int SALARY_WEIGHT = 20;
    private static final int WORK_MODEL_WEIGHT = 15;
    private static final int EDUCATION_WEIGHT = 15;
    private static final int ENGLISH_WEIGHT = 15;

    private CandidateJobAdherenceCalculator() {
    }

    public static Result calculate(JobPosition job, CandidateApplication application) {
        if (job == null || application == null) {
            return new Result(0, List.of());
        }

        Candidate candidate = application.getCandidate();
        Map<String, String> answers = answersByKey(application.getAnswers());
        List<Criterion> criteria = new ArrayList<>();

        addTechnologyCriterion(job, candidate, criteria);
        addSalaryCriterion(job, answers, criteria);
        addWorkModelCriterion(job, answers, criteria);
        addEducationCriterion(job, candidate, answers, criteria);
        addEnglishCriterion(job, candidate, answers, criteria);

        int totalWeight = criteria.stream().mapToInt(Criterion::weight).sum();
        if (totalWeight == 0) {
            return new Result(0, criteria);
        }
        double weightedScore = criteria.stream()
                .mapToDouble(criterion -> criterion.percentage() * criterion.weight())
                .sum();
        int percentage = (int) Math.round(weightedScore / totalWeight);
        return new Result(percentage, criteria);
    }

    private static void addTechnologyCriterion(JobPosition job, Candidate candidate, List<Criterion> criteria) {
        List<Technology> required = job.getTechnologies() == null ? List.of() : job.getTechnologies();
        if (required.isEmpty()) {
            return;
        }
        Set<Long> candidateTechnologyIds = candidate == null || candidate.getTechnologies() == null
                ? Set.of()
                : candidate.getTechnologies().stream()
                .map(Technology::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        List<String> matched = required.stream()
                .filter(technology -> technology.getId() != null && candidateTechnologyIds.contains(technology.getId()))
                .map(Technology::getName)
                .filter(Objects::nonNull)
                .toList();
        int percentage = (int) Math.round(100.0 * matched.size() / required.size());
        String detail = matched.size() + " de " + required.size() + " tecnologias exigidas";
        if (!matched.isEmpty()) {
            detail += ": " + String.join(", ", matched);
        }
        criteria.add(new Criterion("TECHNOLOGIES", "Tecnologias", percentage, TECHNOLOGY_WEIGHT, detail));
    }

    private static void addSalaryCriterion(JobPosition job, Map<String, String> answers, List<Criterion> criteria) {
        BigDecimal ceiling = job.getMaxSalary() != null ? job.getMaxSalary() : job.getMinSalary();
        if (ceiling == null || ceiling.signum() <= 0) {
            return;
        }
        BigDecimal expectation = parseSalary(answers.get(ApplicationFormQuestionKeys.SALARY_EXPECTATION));
        if (expectation == null) {
            criteria.add(new Criterion("SALARY", "Pretensão salarial", 0, SALARY_WEIGHT,
                    "Pretensão salarial não informada"));
            return;
        }

        int percentage = 100;
        if (expectation.compareTo(ceiling) > 0) {
            BigDecimal excess = expectation.subtract(ceiling)
                    .divide(ceiling, 4, RoundingMode.HALF_UP);
            percentage = Math.max(0, (int) Math.round(100 - excess.doubleValue() * 100));
        }
        String detail = "Pretensão R$ " + formatCurrency(expectation) + " · teto da vaga R$ "
                + formatCurrency(ceiling);
        criteria.add(new Criterion("SALARY", "Pretensão salarial", percentage, SALARY_WEIGHT, detail));
    }

    private static void addWorkModelCriterion(JobPosition job, Map<String, String> answers, List<Criterion> criteria) {
        WorkModelEnum required = job.getWorkModel();
        if (required == null) {
            return;
        }
        String answer = answers.get(ApplicationFormQuestionKeys.WORK_MODEL);
        Set<String> accepted = answer == null
                ? Set.of()
                : Arrays.stream(answer.split("[;,/]"))
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
        boolean matches = accepted.contains(required.name());
        String detail = answer == null || answer.isBlank()
                ? "Modelo de trabalho não informado"
                : (matches ? "Aceita " : "Não aceita ") + workModelLabel(required);
        criteria.add(new Criterion("WORK_MODEL", "Modelo de trabalho", matches ? 100 : 0,
                WORK_MODEL_WEIGHT, detail));
    }

    private static void addEducationCriterion(JobPosition job, Candidate candidate, Map<String, String> answers,
                                              List<Criterion> criteria) {
        EducationLevelEnum required = job.getEducationLevel();
        if (required == null) {
            return;
        }
        EducationLevelEnum actual = educationLevel(answers.get(ApplicationFormQuestionKeys.EDUCATION_LEVEL));
        if (actual == null && candidate != null) {
            actual = candidate.getEducationLevel();
        }
        if (actual == null) {
            criteria.add(new Criterion("EDUCATION", "Formação", 0, EDUCATION_WEIGHT,
                    "Formação não informada · exigido " + educationLabel(required)));
            return;
        }
        int difference = required.ordinal() - actual.ordinal();
        int percentage = difference <= 0 ? 100 : Math.max(0, 100 - difference * 35);
        criteria.add(new Criterion("EDUCATION", "Formação", percentage, EDUCATION_WEIGHT,
                educationLabel(actual) + " · exigido " + educationLabel(required)));
    }

    private static void addEnglishCriterion(JobPosition job, Candidate candidate, Map<String, String> answers,
                                            List<Criterion> criteria) {
        EnglishLevelEnum required = job.getEnglishLevel();
        if (required == null || required == EnglishLevelEnum.NONE) {
            return;
        }
        EnglishLevelEnum actual = englishLevel(answers.get(ApplicationFormQuestionKeys.ENGLISH_LEVEL));
        if (actual == null && candidate != null) {
            actual = candidate.getEnglishLevel();
        }
        if (actual == null) {
            criteria.add(new Criterion("ENGLISH", "Inglês", 0, ENGLISH_WEIGHT,
                    "Nível de inglês não informado · exigido " + englishLabel(required)));
            return;
        }
        int difference = required.ordinal() - actual.ordinal();
        int percentage = difference <= 0 ? 100 : (difference == 1 ? 40 : 0);
        criteria.add(new Criterion("ENGLISH", "Inglês", percentage, ENGLISH_WEIGHT,
                englishLabel(actual) + " · exigido " + englishLabel(required)));
    }

    private static Map<String, String> answersByKey(List<FormAnswer> answers) {
        Map<String, String> result = new HashMap<>();
        if (answers == null) {
            return result;
        }
        for (FormAnswer answer : answers) {
            if (answer == null || answer.questionKey() == null || answer.isEmpty()) {
                continue;
            }
            result.put(answer.questionKey(), answer.answer().trim());
        }
        return result;
    }

    private static BigDecimal parseSalary(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replaceAll("[^0-9,.]", "");
        if (normalized.isBlank()) {
            return null;
        }
        if (normalized.contains(",")) {
            normalized = normalized.replace(".", "").replace(",", ".");
        } else if (normalized.matches("\\d{1,3}(\\.\\d{3})+")) {
            normalized = normalized.replace(".", "");
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String formatCurrency(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private static EducationLevelEnum educationLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EducationLevelEnum.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static EnglishLevelEnum englishLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EnglishLevelEnum.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String workModelLabel(WorkModelEnum workModel) {
        return switch (workModel) {
            case REMOTE -> "Remoto";
            case HYBRID -> "Híbrido";
            case ON_SITE -> "Presencial";
        };
    }

    private static String educationLabel(EducationLevelEnum level) {
        return switch (level) {
            case HIGH_SCHOOL -> "Ensino Médio";
            case ASSOCIATE -> "Tecnólogo";
            case BACHELORS -> "Superior Completo";
            case SPECIALIZATION -> "Especialização";
            case MASTERS -> "Mestrado";
            case DOCTORATE -> "Doutorado";
        };
    }

    private static String englishLabel(EnglishLevelEnum level) {
        return switch (level) {
            case NONE -> "Não exigido";
            case BASIC -> "Básico";
            case INTERMEDIATE -> "Intermediário";
            case ADVANCED -> "Avançado";
            case FLUENT -> "Fluente";
        };
    }

    public record Result(int percentage, List<Criterion> criteria) {
    }

    public record Criterion(String key, String label, int percentage, int weight, String detail) {
    }
}
