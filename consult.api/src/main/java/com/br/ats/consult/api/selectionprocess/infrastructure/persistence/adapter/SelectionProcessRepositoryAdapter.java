package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.adapter;

import com.br.ats.consult.api.candidate.domain.exception.CandidateNotFoundException;
import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateEntity;
import com.br.ats.consult.api.candidate.infrastructure.persistence.repository.SpringDataCandidateRepository;
import com.br.ats.consult.api.client.infrastructure.persistence.repository.SpringDataClientRepository;
import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.job.infrastructure.persistence.repository.SpringDataJobPositionRepository;
import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.exception.CandidateApplicationNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.exception.SelectionProcessNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.model.ApplicationComment;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.CandidateApplicationAnswerEntity;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.CandidateApplicationCommentEntity;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.CandidateApplicationEntity;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.SelectionProcessEntity;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.mapper.CandidateApplicationMapper;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.mapper.SelectionProcessMapper;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository.SpringDataCandidateApplicationCommentRepository;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository.SpringDataCandidateApplicationRepository;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository.SpringDataSelectionProcessRepository;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SelectionProcessRepositoryAdapter implements SelectionProcessGateway {

    private final SpringDataSelectionProcessRepository repository;
    private final SpringDataCandidateApplicationRepository applicationRepository;
    private final SpringDataCandidateApplicationCommentRepository applicationCommentRepository;
    private final SpringDataClientRepository clientRepository;
    private final SpringDataJobPositionRepository jobPositionRepository;
    private final JobPositionGateway jobPositionGateway;
    private final SpringDataCandidateRepository candidateRepository;
    private final TechnologyGateway technologyGateway;
    private final SelectionProcessMapper mapper;
    private final CandidateApplicationMapper applicationMapper;

    public SelectionProcessRepositoryAdapter(SpringDataSelectionProcessRepository repository,
                                             SpringDataCandidateApplicationRepository applicationRepository,
                                             SpringDataCandidateApplicationCommentRepository applicationCommentRepository,
                                             SpringDataClientRepository clientRepository,
                                             SpringDataJobPositionRepository jobPositionRepository,
                                             JobPositionGateway jobPositionGateway,
                                             SpringDataCandidateRepository candidateRepository,
                                             TechnologyGateway technologyGateway,
                                             SelectionProcessMapper mapper,
                                             CandidateApplicationMapper applicationMapper) {
        this.repository = repository;
        this.applicationRepository = applicationRepository;
        this.applicationCommentRepository = applicationCommentRepository;
        this.clientRepository = clientRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.jobPositionGateway = jobPositionGateway;
        this.candidateRepository = candidateRepository;
        this.technologyGateway = technologyGateway;
        this.mapper = mapper;
        this.applicationMapper = applicationMapper;
    }

    @Override
    public SelectionProcess save(SelectionProcess process) {
        SelectionProcessEntity entity = process.getId() == null
                ? new SelectionProcessEntity()
                : repository.findById(process.getId())
                .orElseThrow(() -> new SelectionProcessNotFoundException(process.getId()));

        entity.setClient(clientRepository.findById(process.getClient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente inválido.")));
        entity.setJobPosition(jobPositionRepository.findById(process.getJobPosition().getId())
                .orElseThrow(() -> new JobPositionNotFoundException(process.getJobPosition().getId())));
        entity.setStatus(process.getStatus());
        entity.setOpenedAt(process.getOpenedAt());

        return enrichJobPosition(mapper.toDomain(repository.save(entity)));
    }

    @Override
    public Optional<SelectionProcess> findById(Long id) {
        return repository.findByIdWithDetails(id)
                .map(mapper::toDomain)
                .map(this::enrichJobPosition);
    }

    @Override
    public Page<SelectionProcess> findAll(Pageable pageable) {
        Page<SelectionProcessEntity> page = repository.findAllWithDetails(pageable);
        return new PageImpl<>(
                page.getContent().stream()
                        .map(mapper::toDomain)
                        .map(this::enrichJobPosition)
                        .toList(),
                pageable,
                page.getTotalElements()
        );
    }

    @Override
    public Optional<SelectionProcess> findOpenByClientIdAndJobPositionId(Long clientId, Long jobPositionId) {
        return repository.findByClientAndJobAndStatus(clientId, jobPositionId, SelectionProcessStatusEnum.OPEN)
                .map(mapper::toDomain)
                .map(this::enrichJobPosition);
    }

    private SelectionProcess enrichJobPosition(SelectionProcess process) {
        if (process.getJobPosition() == null || process.getJobPosition().getId() == null) {
            return process;
        }
        jobPositionGateway.findById(process.getJobPosition().getId())
                .ifPresent(process::setJobPosition);
        return process;
    }

    @Override
    public CandidateApplication addApplication(CandidateApplication application) {
        SelectionProcessEntity process = repository.findById(application.getSelectionProcessId())
                .orElseThrow(() -> new SelectionProcessNotFoundException(application.getSelectionProcessId()));
        CandidateEntity candidate = candidateRepository.findById(application.getCandidate().getId())
                .orElseThrow(() -> new CandidateNotFoundException(application.getCandidate().getId()));

        CandidateApplicationEntity entity = CandidateApplicationEntity.builder()
                .process(process)
                .candidate(candidate)
                .resumeFilename(application.getResume().getFilename())
                .resumeContentType(application.getResume().getContentType())
                .resumeSizeBytes(application.getResume().getSizeBytes())
                .resumeStorageKey(application.getResume().getStorageKey())
                .status(application.getStatus() == null
                        ? CandidateApplicationStatusEnum.TRIAGEM
                        : application.getStatus())
                .appliedAt(application.getAppliedAt() == null ? Instant.now() : application.getAppliedAt())
                .consentAccepted(application.isConsentAccepted())
                .consentAcceptedAt(application.getConsentAcceptedAt())
                .consentVersion(application.getConsentVersion())
                .consentIp(application.getConsentIp())
                .build();

        if (application.getAnswers() != null) {
            application.getAnswers().forEach(answer -> entity.getAnswers().add(
                    CandidateApplicationAnswerEntity.builder()
                            .application(entity)
                            .questionId(answer.questionId())
                            .questionKey(answer.questionKey())
                            .questionPrompt(answer.questionPrompt())
                            .answer(answer.answer())
                            .position(answer.position() == null ? 0 : answer.position())
                            .build()
            ));
        }

        return toDomain(applicationRepository.save(entity));
    }

    @Override
    public List<CandidateApplication> findApplicationsByProcessId(Long processId) {
        return toDomainList(applicationRepository.findByProcessIdOrderByAppliedAtDesc(processId));
    }

    @Override
    public Optional<CandidateApplication> findApplicationById(Long applicationId) {
        return applicationRepository.findWithDetailsById(applicationId)
                .map(this::toDomain);
    }

    @Override
    public CandidateApplication updateApplicationStatus(Long applicationId,
                                                        CandidateApplicationStatusEnum status) {
        CandidateApplicationEntity entity = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CandidateApplicationNotFoundException(applicationId));
        entity.setStatus(status);
        return toDomain(applicationRepository.save(entity));
    }

    private CandidateApplication toDomain(CandidateApplicationEntity entity) {
        CandidateApplication application = applicationMapper.toDomain(entity);
        mapCandidateTechnologies(application, entity, loadTechnologies(List.of(entity)));
        return application;
    }

    private List<CandidateApplication> toDomainList(List<CandidateApplicationEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        Map<Long, Technology> technologiesById = loadTechnologies(entities);
        return entities.stream()
                .map(entity -> {
                    CandidateApplication application = applicationMapper.toDomain(entity);
                    mapCandidateTechnologies(application, entity, technologiesById);
                    return application;
                })
                .toList();
    }

    private Map<Long, Technology> loadTechnologies(List<CandidateApplicationEntity> entities) {
        Set<Long> technologyIds = entities.stream()
                .map(CandidateApplicationEntity::getCandidate)
                .filter(Objects::nonNull)
                .map(CandidateEntity::getTechnologyIds)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (technologyIds.isEmpty()) {
            return Map.of();
        }
        return technologyGateway.findAllByIds(new ArrayList<>(technologyIds)).stream()
                .collect(Collectors.toMap(Technology::getId, Function.identity()));
    }

    private void mapCandidateTechnologies(CandidateApplication application, CandidateApplicationEntity entity,
                                          Map<Long, Technology> technologiesById) {
        if (application == null || application.getCandidate() == null
                || entity == null || entity.getCandidate() == null) {
            return;
        }
        List<Long> technologyIds = entity.getCandidate().getTechnologyIds();
        List<Technology> technologies = technologyIds == null
                ? List.of()
                : technologyIds.stream()
                .map(technologiesById::get)
                .filter(Objects::nonNull)
                .toList();
        application.getCandidate().setTechnologies(new ArrayList<>(technologies));
    }

    @Override
    public ApplicationComment addComment(ApplicationComment comment) {
        CandidateApplicationEntity application = applicationRepository.findById(comment.getApplicationId())
                .orElseThrow(() -> new CandidateApplicationNotFoundException(comment.getApplicationId()));

        CandidateApplicationCommentEntity entity = CandidateApplicationCommentEntity.builder()
                .application(application)
                .authorId(comment.getAuthorId())
                .authorEmail(comment.getAuthorEmail())
                .message(comment.getMessage().trim())
                .createdAt(comment.getCreatedAt() == null ? Instant.now() : comment.getCreatedAt())
                .build();

        CandidateApplicationCommentEntity saved = applicationCommentRepository.save(entity);
        return applicationMapper.mapComment(saved);
    }

    @Override
    public List<ApplicationComment> findCommentsByApplicationId(Long applicationId) {
        return applicationRepository.findWithDetailsById(applicationId)
                .map(CandidateApplicationEntity::getComments)
                .orElseThrow(() -> new CandidateApplicationNotFoundException(applicationId))
                .stream()
                .map(applicationMapper::mapComment)
                .toList();
    }

    @Override
    public boolean existsApplication(Long processId, Long candidateId) {
        return applicationRepository.existsByProcessIdAndCandidateId(processId, candidateId);
    }
}
