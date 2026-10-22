package com.br.ats.consult.api.selectionprocess.domain.usecase.impl;

import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.client.domain.exception.ClientNotFoundException;
import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.client.domain.port.out.ClientGateway;
import com.br.ats.consult.api.job.domain.enums.JobStatusEnum;
import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.exception.CandidateAlreadyInProcessException;
import com.br.ats.consult.api.selectionprocess.domain.exception.InvalidResumeException;
import com.br.ats.consult.api.selectionprocess.domain.exception.SelectionProcessNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.exception.SelectionProcessNotOpenException;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplicationResult;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeMetadata;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.domain.port.out.ResumeStorage;
import com.br.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import com.br.ats.consult.api.selectionprocess.domain.usecase.ApplyCandidateUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public class ApplyCandidateUseCaseImpl implements ApplyCandidateUseCase {

    private final SelectionProcessGateway processGateway;
    private final CandidateGateway candidateGateway;
    private final ClientGateway clientGateway;
    private final JobPositionGateway jobPositionGateway;
    private final ResumeStorage resumeStorage;

    public ApplyCandidateUseCaseImpl(SelectionProcessGateway processGateway, CandidateGateway candidateGateway,
                                     ClientGateway clientGateway, JobPositionGateway jobPositionGateway,
                                     ResumeStorage resumeStorage) {
        this.processGateway = processGateway;
        this.candidateGateway = candidateGateway;
        this.clientGateway = clientGateway;
        this.jobPositionGateway = jobPositionGateway;
        this.resumeStorage = resumeStorage;
    }

    @Override
    @Transactional
    public CandidateApplicationResult applyToProcess(Long processId, Candidate candidate, ResumeUpload resume) {
        SelectionProcess process = processGateway.findById(processId)
                .orElseThrow(() -> new SelectionProcessNotFoundException(processId));
        if (!process.isOpen()) {
            throw new SelectionProcessNotOpenException(processId);
        }
        return addApplication(process, candidate, resume);
    }

    @Override
    @Transactional
    public CandidateApplicationResult applyToJob(Long jobId, Long clientId, Candidate candidate, ResumeUpload resume) {
        JobPosition job = jobPositionGateway.findById(jobId)
                .orElseThrow(() -> new JobPositionNotFoundException(jobId));
        if (job.getStatus() != JobStatusEnum.OPEN) {
            throw new IllegalArgumentException("A vaga não está aberta para receber candidaturas.");
        }

        Client client = clientGateway.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        SelectionProcess process = processGateway.findOpenByClientIdAndJobPositionId(clientId, jobId)
                .orElseGet(() -> processGateway.save(SelectionProcess.builder()
                        .client(client)
                        .jobPosition(job)
                        .status(SelectionProcessStatusEnum.OPEN)
                        .openedAt(LocalDate.now())
                        .build()));

        return addApplication(process, candidate, resume);
    }

    private CandidateApplicationResult addApplication(SelectionProcess process, Candidate candidate,
                                                      ResumeUpload resume) {
        if (resume == null) {
            throw new InvalidResumeException("O currículo em PDF é obrigatório.");
        }

        candidate.normalize();
        candidate.validate();

        Candidate persistedCandidate = candidateGateway.findByEmail(candidate.getEmail())
                .map(existing -> {
                    existing.updateFrom(candidate);
                    return candidateGateway.save(existing);
                })
                .orElseGet(() -> candidateGateway.save(candidate));

        if (processGateway.existsApplication(process.getId(), persistedCandidate.getId())) {
            throw new CandidateAlreadyInProcessException(persistedCandidate.getEmail());
        }

        ResumeMetadata storedResume = resumeStorage.store(resume);
        try {
            CandidateApplication application = CandidateApplication.builder()
                    .selectionProcessId(process.getId())
                    .candidate(persistedCandidate)
                    .resume(storedResume)
                    .build();
            application.validate();

            CandidateApplication saved = processGateway.addApplication(application);
            return new CandidateApplicationResult(process, saved);
        } catch (RuntimeException ex) {
            resumeStorage.delete(storedResume.getStorageKey());
            throw ex;
        }
    }
}
