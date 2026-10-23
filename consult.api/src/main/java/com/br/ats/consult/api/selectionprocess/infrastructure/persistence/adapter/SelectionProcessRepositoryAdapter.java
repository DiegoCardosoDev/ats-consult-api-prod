package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.adapter;

import com.br.ats.consult.api.candidate.domain.exception.CandidateNotFoundException;
import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateEntity;
import com.br.ats.consult.api.candidate.infrastructure.persistence.repository.SpringDataCandidateRepository;
import com.br.ats.consult.api.client.infrastructure.persistence.repository.SpringDataClientRepository;
import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.job.infrastructure.persistence.repository.SpringDataJobPositionRepository;
import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.exception.SelectionProcessNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.CandidateApplicationAnswerEntity;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.CandidateApplicationEntity;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.SelectionProcessEntity;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.mapper.CandidateApplicationMapper;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.mapper.SelectionProcessMapper;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository.SpringDataCandidateApplicationRepository;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository.SpringDataSelectionProcessRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class SelectionProcessRepositoryAdapter implements SelectionProcessGateway {

    private final SpringDataSelectionProcessRepository repository;
    private final SpringDataCandidateApplicationRepository applicationRepository;
    private final SpringDataClientRepository clientRepository;
    private final SpringDataJobPositionRepository jobPositionRepository;
    private final SpringDataCandidateRepository candidateRepository;
    private final SelectionProcessMapper mapper;
    private final CandidateApplicationMapper applicationMapper;

    public SelectionProcessRepositoryAdapter(SpringDataSelectionProcessRepository repository,
                                             SpringDataCandidateApplicationRepository applicationRepository,
                                             SpringDataClientRepository clientRepository,
                                             SpringDataJobPositionRepository jobPositionRepository,
                                             SpringDataCandidateRepository candidateRepository,
                                             SelectionProcessMapper mapper,
                                             CandidateApplicationMapper applicationMapper) {
        this.repository = repository;
        this.applicationRepository = applicationRepository;
        this.clientRepository = clientRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.candidateRepository = candidateRepository;
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

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<SelectionProcess> findById(Long id) {
        return repository.findByIdWithDetails(id).map(mapper::toDomain);
    }

    @Override
    public Page<SelectionProcess> findAll(Pageable pageable) {
        Page<SelectionProcessEntity> page = repository.findAllWithDetails(pageable);
        return new PageImpl<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                pageable,
                page.getTotalElements()
        );
    }

    @Override
    public Optional<SelectionProcess> findOpenByClientIdAndJobPositionId(Long clientId, Long jobPositionId) {
        return repository.findByClientAndJobAndStatus(clientId, jobPositionId, SelectionProcessStatusEnum.OPEN)
                .map(mapper::toDomain);
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
                .appliedAt(application.getAppliedAt() == null ? Instant.now() : application.getAppliedAt())
                .build();

        if (application.getAnswers() != null) {
            application.getAnswers().forEach(answer -> entity.getAnswers().add(
                    CandidateApplicationAnswerEntity.builder()
                            .application(entity)
                            .questionId(answer.questionId())
                            .questionPrompt(answer.questionPrompt())
                            .answer(answer.answer())
                            .position(answer.position() == null ? 0 : answer.position())
                            .build()
            ));
        }

        return applicationMapper.toDomain(applicationRepository.save(entity));
    }

    @Override
    public List<CandidateApplication> findApplicationsByProcessId(Long processId) {
        return applicationRepository.findByProcessIdOrderByAppliedAtDesc(processId).stream()
                .map(applicationMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<CandidateApplication> findApplicationById(Long applicationId) {
        return applicationRepository.findWithDetailsById(applicationId)
                .map(applicationMapper::toDomain);
    }

    @Override
    public boolean existsApplication(Long processId, Long candidateId) {
        return applicationRepository.existsByProcessIdAndCandidateId(processId, candidateId);
    }
}
