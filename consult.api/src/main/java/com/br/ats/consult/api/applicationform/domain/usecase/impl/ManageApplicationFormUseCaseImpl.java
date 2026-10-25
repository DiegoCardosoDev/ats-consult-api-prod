package com.br.ats.consult.api.applicationform.domain.usecase.impl;

import com.br.ats.consult.api.applicationform.domain.exception.ApplicationFormNotFoundException;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestion;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestionKeys;
import com.br.ats.consult.api.applicationform.domain.port.out.ApplicationFormGateway;
import com.br.ats.consult.api.applicationform.domain.service.ApplicationFormTemplateBuilder;
import com.br.ats.consult.api.applicationform.domain.usecase.ManageApplicationFormUseCase;
import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ManageApplicationFormUseCaseImpl implements ManageApplicationFormUseCase {

    private final ApplicationFormGateway formGateway;
    private final JobPositionGateway jobPositionGateway;
    private final TechnologyGateway technologyGateway;
    private final ApplicationFormTemplateBuilder templateBuilder;

    public ManageApplicationFormUseCaseImpl(ApplicationFormGateway formGateway,
                                            JobPositionGateway jobPositionGateway,
                                            TechnologyGateway technologyGateway) {
        this.formGateway = formGateway;
        this.jobPositionGateway = jobPositionGateway;
        this.technologyGateway = technologyGateway;
        this.templateBuilder = new ApplicationFormTemplateBuilder();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationForm> findByJobPositionId(Long jobPositionId) {
        ensureJobExists(jobPositionId);
        return formGateway.findByJobPositionId(jobPositionId);
    }

    @Override
    @Transactional
    public ApplicationForm resolveForJob(Long jobPositionId) {
        JobPosition job = findJob(jobPositionId);
        Optional<ApplicationForm> existing = formGateway.findByJobPositionId(jobPositionId)
                .filter(form -> form.isActive() && form.hasQuestions());
        if (existing.isPresent()) {
            ApplicationForm form = existing.get();
            boolean changed = ensureCoreQuestions(form);
            changed |= ensureTechnologyQuestions(form, job);
            if (changed) {
                form.validate();
                return formGateway.save(form);
            }
            return form;
        }
        return generateForJob(job);
    }

    @Override
    @Transactional
    public ApplicationForm save(ApplicationForm form) {
        JobPosition job = findJob(form.getJobPositionId());
        form.validate();
        ensureCoreQuestions(form);
        ensureTechnologyQuestions(form, job);
        form.validate();

        formGateway.findByJobPositionId(form.getJobPositionId())
                .ifPresent(existing -> form.setId(existing.getId()));

        return formGateway.save(form);
    }

    @Override
    @Transactional
    public ApplicationForm generateForJob(Long jobPositionId) {
        return generateForJob(findJob(jobPositionId));
    }

    private ApplicationForm generateForJob(JobPosition job) {
        ApplicationForm generated = templateBuilder.build(job.getId(), job.getTitle(),
                job.getTechnologies(), technologyGateway.findAll());
        generated.validate();

        formGateway.findByJobPositionId(job.getId())
                .ifPresent(existing -> generated.setId(existing.getId()));

        return formGateway.save(generated);
    }

    @Override
    @Transactional
    public void deleteByJobPositionId(Long jobPositionId) {
        ensureJobExists(jobPositionId);
        if (!formGateway.existsByJobPositionId(jobPositionId)) {
            throw new ApplicationFormNotFoundException(jobPositionId);
        }
        formGateway.deleteByJobPositionId(jobPositionId);
    }

    private JobPosition findJob(Long jobPositionId) {
        if (jobPositionId == null) {
            throw new JobPositionNotFoundException(jobPositionId);
        }
        return jobPositionGateway.findById(jobPositionId)
                .orElseThrow(() -> new JobPositionNotFoundException(jobPositionId));
    }

    private void ensureJobExists(Long jobPositionId) {
        findJob(jobPositionId);
    }

    private boolean ensureCoreQuestions(ApplicationForm form) {
        List<ApplicationFormQuestion> coreQuestions = templateBuilder.buildCoreQuestions();
        Set<String> keys = form.getQuestions().stream()
                .map(ApplicationFormQuestion::getQuestionKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<ApplicationFormQuestion> questions = new ArrayList<>(form.getQuestions());
        boolean changed = false;
        for (ApplicationFormQuestion core : coreQuestions) {
            if (!keys.contains(core.getQuestionKey())) {
                questions.add(core);
                changed = true;
            }
        }

        Map<String, ApplicationFormQuestion> coreByKey = coreQuestions.stream()
                .collect(Collectors.toMap(ApplicationFormQuestion::getQuestionKey, question -> question));
        for (ApplicationFormQuestion question : questions) {
            ApplicationFormQuestion core = coreByKey.get(question.getQuestionKey());
            if (core == null) {
                continue;
            }
            if (question.getType() != core.getType()) {
                question.setType(core.getType());
                changed = true;
            }
            if (question.isRequired() != core.isRequired()) {
                question.setRequired(core.isRequired());
                changed = true;
            }
            if (!question.isSystemGenerated()) {
                question.setSystemGenerated(true);
                changed = true;
            }
        }

        if (!changed) {
            return false;
        }
        applyPositions(questions);
        form.setQuestions(questions);
        return true;
    }

    private void applyPositions(List<ApplicationFormQuestion> questions) {
        questions.sort(Comparator
                .comparingInt(this::questionSortOrder)
                .thenComparingInt(question -> question.getPosition() == null
                        ? Integer.MAX_VALUE
                        : question.getPosition()));
        for (int index = 0; index < questions.size(); index++) {
            questions.get(index).setPosition(index);
        }
    }

    private boolean ensureTechnologyQuestions(ApplicationForm form, JobPosition job) {
        List<ApplicationFormQuestion> standardTechnologyQuestions = templateBuilder
                .build(job.getId(), job.getTitle(), job.getTechnologies(), technologyGateway.findAll())
                .getQuestions().stream()
                .filter(this::isTechnologyQuestion)
                .toList();
        if (standardTechnologyQuestions.isEmpty()) {
            return false;
        }

        Map<String, ApplicationFormQuestion> existingByKey = form.getQuestions().stream()
                .filter(question -> question.getQuestionKey() != null)
                .collect(Collectors.toMap(ApplicationFormQuestion::getQuestionKey, question -> question,
                        (first, second) -> first));
        List<ApplicationFormQuestion> questions = new ArrayList<>(form.getQuestions());
        boolean changed = false;
        for (ApplicationFormQuestion standard : standardTechnologyQuestions) {
            ApplicationFormQuestion existing = existingByKey.get(standard.getQuestionKey());
            if (existing == null) {
                questions.add(standard);
                changed = true;
                continue;
            }
            if (existing.getType() != standard.getType()
                    || !existing.isRequired()
                    || !existing.isSystemGenerated()) {
                existing.setType(standard.getType());
                existing.setRequired(true);
                existing.setSystemGenerated(true);
                changed = true;
            }
        }

        if (!changed) {
            return false;
        }
        applyPositions(questions);
        form.setQuestions(questions);
        return true;
    }

    private boolean isTechnologyQuestion(ApplicationFormQuestion question) {
        String key = question.getQuestionKey();
        return key != null && (key.startsWith(ApplicationFormQuestionKeys.TECHNOLOGY_CATEGORY_PREFIX)
                || key.startsWith(ApplicationFormQuestionKeys.TECHNOLOGY_CHILDREN_PREFIX));
    }

    private int questionSortOrder(ApplicationFormQuestion question) {
        if (question.getQuestionKey() == null) {
            return Integer.MAX_VALUE;
        }
        int index = ApplicationFormQuestionKeys.CORE_KEYS.indexOf(question.getQuestionKey());
        return index >= 0 ? index : Integer.MAX_VALUE;
    }
}
