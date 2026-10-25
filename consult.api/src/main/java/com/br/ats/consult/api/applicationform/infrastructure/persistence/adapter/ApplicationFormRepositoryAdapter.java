package com.br.ats.consult.api.applicationform.infrastructure.persistence.adapter;

import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestion;
import com.br.ats.consult.api.applicationform.domain.port.out.ApplicationFormGateway;
import com.br.ats.consult.api.applicationform.infrastructure.persistence.entity.ApplicationFormEntity;
import com.br.ats.consult.api.applicationform.infrastructure.persistence.entity.ApplicationFormQuestionEntity;
import com.br.ats.consult.api.applicationform.infrastructure.persistence.repository.SpringDataApplicationFormRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ApplicationFormRepositoryAdapter implements ApplicationFormGateway {

    private final SpringDataApplicationFormRepository repository;

    public ApplicationFormRepositoryAdapter(SpringDataApplicationFormRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationForm> findByJobPositionId(Long jobPositionId) {
        return repository.findByJobPositionId(jobPositionId).map(this::toDomain);
    }

    @Override
    @Transactional
    public ApplicationForm save(ApplicationForm form) {
        ApplicationFormEntity entity = form.getId() == null
                ? new ApplicationFormEntity()
                : repository.findById(form.getId()).orElseGet(ApplicationFormEntity::new);

        entity.setJobPositionId(form.getJobPositionId());
        entity.setTitle(form.getTitle());
        entity.setDescription(form.getDescription());
        entity.setActive(form.isActive());
        entity.replaceQuestions(toQuestionEntities(form.getQuestions()));

        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteByJobPositionId(Long jobPositionId) {
        repository.findByJobPositionId(jobPositionId)
                .ifPresent(repository::delete);
    }

    @Override
    public boolean existsByJobPositionId(Long jobPositionId) {
        return repository.existsByJobPositionId(jobPositionId);
    }

    private List<ApplicationFormQuestionEntity> toQuestionEntities(List<ApplicationFormQuestion> questions) {
        if (questions == null) {
            return List.of();
        }
        List<ApplicationFormQuestionEntity> entities = new ArrayList<>();
        for (int index = 0; index < questions.size(); index++) {
            ApplicationFormQuestion question = questions.get(index);
            entities.add(ApplicationFormQuestionEntity.builder()
                    .prompt(question.getPrompt())
                    .type(question.getType())
                    .required(question.isRequired())
                    .position(question.getPosition() == null ? index : question.getPosition())
                    .questionKey(question.getQuestionKey())
                    .technologyCategory(question.getTechnologyCategory())
                    .parentTechnologyId(question.getParentTechnologyId())
                    .maxSelections(question.getMaxSelections())
                    .systemGenerated(question.isSystemGenerated())
                    .options(new ArrayList<>(question.normalizedOptions()))
                    .build());
        }
        return entities;
    }

    private ApplicationForm toDomain(ApplicationFormEntity entity) {
        List<ApplicationFormQuestion> questions = entity.getQuestions() == null
                ? List.of()
                : entity.getQuestions().stream()
                .map(question -> ApplicationFormQuestion.builder()
                        .id(question.getId())
                        .prompt(question.getPrompt())
                        .type(question.getType())
                        .required(question.isRequired())
                        .position(question.getPosition())
                        .questionKey(question.getQuestionKey())
                        .technologyCategory(question.getTechnologyCategory())
                        .parentTechnologyId(question.getParentTechnologyId())
                        .maxSelections(question.getMaxSelections())
                        .systemGenerated(question.isSystemGenerated())
                        .options(question.getOptions() == null ? new ArrayList<>() : new ArrayList<>(question.getOptions()))
                        .build())
                .toList();

        return ApplicationForm.builder()
                .id(entity.getId())
                .jobPositionId(entity.getJobPositionId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .active(entity.isActive())
                .questions(new ArrayList<>(questions))
                .build();
    }
}
