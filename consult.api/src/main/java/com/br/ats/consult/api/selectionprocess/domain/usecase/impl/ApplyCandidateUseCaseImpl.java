package com.br.ats.consult.api.selectionprocess.domain.usecase.impl;

import com.br.ats.consult.api.applicationform.domain.enums.QuestionTypeEnum;
import com.br.ats.consult.api.applicationform.domain.exception.InvalidApplicationAnswersException;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestion;
import com.br.ats.consult.api.applicationform.domain.model.FormAnswer;
import com.br.ats.consult.api.applicationform.domain.model.FormAnswerInput;
import com.br.ats.consult.api.applicationform.domain.port.out.ApplicationFormGateway;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.client.domain.model.Client;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplyCandidateUseCaseImpl implements ApplyCandidateUseCase {

    private final SelectionProcessGateway processGateway;
    private final CandidateGateway candidateGateway;
    private final JobPositionGateway jobPositionGateway;
    private final ApplicationFormGateway formGateway;
    private final ResumeStorage resumeStorage;

    public ApplyCandidateUseCaseImpl(SelectionProcessGateway processGateway, CandidateGateway candidateGateway,
                                     JobPositionGateway jobPositionGateway,
                                     ApplicationFormGateway formGateway, ResumeStorage resumeStorage) {
        this.processGateway = processGateway;
        this.candidateGateway = candidateGateway;
        this.jobPositionGateway = jobPositionGateway;
        this.formGateway = formGateway;
        this.resumeStorage = resumeStorage;
    }

    @Override
    @Transactional
    public CandidateApplicationResult applyToProcess(Long processId, Candidate candidate, ResumeUpload resume,
                                                     List<FormAnswerInput> answers) {
        SelectionProcess process = processGateway.findById(processId)
                .orElseThrow(() -> new SelectionProcessNotFoundException(processId));
        if (!process.isOpen()) {
            throw new SelectionProcessNotOpenException(processId);
        }
        List<FormAnswer> validatedAnswers = resolveAnswers(process.getJobPosition().getId(), answers);
        return addApplication(process, candidate, resume, validatedAnswers);
    }

    @Override
    @Transactional
    public CandidateApplicationResult applyToJob(Long jobId, Candidate candidate, ResumeUpload resume,
                                                 List<FormAnswerInput> answers) {
        JobPosition job = jobPositionGateway.findById(jobId)
                .orElseThrow(() -> new JobPositionNotFoundException(jobId));
        if (job.getStatus() != JobStatusEnum.OPEN) {
            throw new IllegalArgumentException("A vaga não está aberta para receber candidaturas.");
        }

        Client client = job.getClient();
        if (client == null || client.getId() == null) {
            throw new IllegalArgumentException("A vaga não possui um cliente associado.");
        }

        SelectionProcess process = processGateway.findOpenByClientIdAndJobPositionId(client.getId(), jobId)
                .orElseGet(() -> processGateway.save(SelectionProcess.builder()
                        .client(client)
                        .jobPosition(job)
                        .status(SelectionProcessStatusEnum.OPEN)
                        .openedAt(LocalDate.now())
                        .build()));

        List<FormAnswer> validatedAnswers = resolveAnswers(jobId, answers);
        return addApplication(process, candidate, resume, validatedAnswers);
    }

    private CandidateApplicationResult addApplication(SelectionProcess process, Candidate candidate,
                                                      ResumeUpload resume, List<FormAnswer> answers) {
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
                    .answers(answers == null ? List.of() : answers)
                    .build();
            application.validate();

            CandidateApplication saved = processGateway.addApplication(application);
            return new CandidateApplicationResult(process, saved);
        } catch (RuntimeException ex) {
            resumeStorage.delete(storedResume.getStorageKey());
            throw ex;
        }
    }

    private List<FormAnswer> resolveAnswers(Long jobPositionId, List<FormAnswerInput> submittedAnswers) {
        if (jobPositionId == null) {
            return List.of();
        }
        ApplicationForm form = formGateway.findByJobPositionId(jobPositionId)
                .filter(ApplicationForm::isActive)
                .orElse(null);
        if (form == null || !form.hasQuestions()) {
            return List.of();
        }

        Map<Long, String> submitted = new HashMap<>();
        if (submittedAnswers != null) {
            submittedAnswers.stream()
                    .filter(answer -> answer.questionId() != null)
                    .forEach(answer -> submitted.put(answer.questionId(),
                            answer.answer() == null ? null : answer.answer().trim()));
        }

        List<FormAnswer> resolved = new ArrayList<>();
        for (ApplicationFormQuestion question : form.getQuestions()) {
            String value = submitted.get(question.getId());
            if (question.isRequired() && (value == null || value.isBlank())) {
                throw new InvalidApplicationAnswersException(
                        "É obrigatório responder à pergunta: " + question.getPrompt());
            }
            if (value == null || value.isBlank()) {
                continue;
            }
            if (question.getType() == QuestionTypeEnum.SINGLE_CHOICE
                    && !question.normalizedOptions().contains(value)) {
                throw new InvalidApplicationAnswersException(
                        "A resposta da pergunta '" + question.getPrompt() + "' não é uma opção válida.");
            }
            resolved.add(new FormAnswer(question.getId(), question.getPrompt(), value,
                    question.getPosition()));
        }
        return resolved;
    }
}
