package com.br.ats.consult.api.selectionprocess.domain.usecase.impl;

import com.br.ats.consult.api.applicationform.domain.enums.QuestionTypeEnum;
import com.br.ats.consult.api.applicationform.domain.exception.InvalidApplicationAnswersException;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestion;
import com.br.ats.consult.api.applicationform.domain.model.FormAnswer;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestionKeys;
import com.br.ats.consult.api.applicationform.domain.model.FormAnswerInput;
import com.br.ats.consult.api.applicationform.domain.usecase.ManageApplicationFormUseCase;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.model.CandidateAddress;
import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.job.domain.enums.EducationLevelEnum;
import com.br.ats.consult.api.job.domain.enums.EnglishLevelEnum;
import com.br.ats.consult.api.job.domain.enums.JobStatusEnum;
import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.exception.CandidateAlreadyInProcessException;
import com.br.ats.consult.api.selectionprocess.domain.exception.PrivacyConsentRequiredException;
import com.br.ats.consult.api.selectionprocess.domain.exception.InvalidResumeException;
import com.br.ats.consult.api.selectionprocess.domain.exception.SelectionProcessNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.exception.SelectionProcessNotOpenException;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplicationResult;
import com.br.ats.consult.api.selectionprocess.domain.model.PrivacyConsentInput;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeMetadata;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.domain.port.out.ResumeStorage;
import com.br.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import com.br.ats.consult.api.selectionprocess.domain.usecase.ApplyCandidateUseCase;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplyCandidateUseCaseImpl implements ApplyCandidateUseCase {

    private final SelectionProcessGateway processGateway;
    private final CandidateGateway candidateGateway;
    private final JobPositionGateway jobPositionGateway;
    private final ManageApplicationFormUseCase applicationFormUseCase;
    private final ResumeStorage resumeStorage;
    private final TechnologyGateway technologyGateway;

    public ApplyCandidateUseCaseImpl(SelectionProcessGateway processGateway, CandidateGateway candidateGateway,
                                     JobPositionGateway jobPositionGateway,
                                     ManageApplicationFormUseCase applicationFormUseCase,
                                     ResumeStorage resumeStorage, TechnologyGateway technologyGateway) {
        this.processGateway = processGateway;
        this.candidateGateway = candidateGateway;
        this.jobPositionGateway = jobPositionGateway;
        this.applicationFormUseCase = applicationFormUseCase;
        this.resumeStorage = resumeStorage;
        this.technologyGateway = technologyGateway;
    }

    @Override
    @Transactional
    public CandidateApplicationResult applyToProcess(Long processId, Candidate candidate, ResumeUpload resume,
                                                     List<FormAnswerInput> answers, PrivacyConsentInput consent) {
        SelectionProcess process = processGateway.findById(processId)
                .orElseThrow(() -> new SelectionProcessNotFoundException(processId));
        if (!process.isOpen()) {
            throw new SelectionProcessNotOpenException(processId);
        }
        ResolvedAnswers resolved = resolveAnswers(process.getJobPosition().getId(), answers);
        applyProfileAnswers(candidate, resolved);
        return addApplication(process, candidate, resume, resolved.answers(), consent);
    }

    @Override
    @Transactional
    public CandidateApplicationResult applyToJob(Long jobId, Candidate candidate, ResumeUpload resume,
                                                 List<FormAnswerInput> answers, PrivacyConsentInput consent) {
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

        ResolvedAnswers resolved = resolveAnswers(jobId, answers);
        applyProfileAnswers(candidate, resolved);
        return addApplication(process, candidate, resume, resolved.answers(), consent);
    }

    private CandidateApplicationResult addApplication(SelectionProcess process, Candidate candidate,
                                                      ResumeUpload resume, List<FormAnswer> answers,
                                                      PrivacyConsentInput consent) {
        validatePrivacyConsent(consent);
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
            applyPrivacyConsent(application, consent);
            application.validate();

            CandidateApplication saved = processGateway.addApplication(application);
            return new CandidateApplicationResult(process, saved);
        } catch (RuntimeException ex) {
            resumeStorage.delete(storedResume.getStorageKey());
            throw ex;
        }
    }

    private ResolvedAnswers resolveAnswers(Long jobPositionId, List<FormAnswerInput> submittedAnswers) {
        if (jobPositionId == null) {
            return new ResolvedAnswers(null, List.of());
        }
        ApplicationForm form = applicationFormUseCase.resolveForJob(jobPositionId);
        if (!form.hasQuestions()) {
            return new ResolvedAnswers(form, List.of());
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
            validateChoiceAnswer(question, value);
            resolved.add(new FormAnswer(question.getId(), question.getQuestionKey(), question.getPrompt(), value,
                    question.getPosition()));
        }
        return new ResolvedAnswers(form, resolved);
    }

    private void validatePrivacyConsent(PrivacyConsentInput consent) {
        if (consent == null || !consent.accepted()) {
            throw new PrivacyConsentRequiredException();
        }
        if (consent.version() == null || consent.version().isBlank()) {
            throw new PrivacyConsentRequiredException(
                    "A versão da Política de Privacidade é obrigatória.");
        }
    }

    private void applyPrivacyConsent(CandidateApplication application, PrivacyConsentInput consent) {
        application.setConsentAccepted(true);
        application.setConsentAcceptedAt(Instant.now());
        application.setConsentVersion(consent.version().trim());
        application.setConsentIp(consent.ipAddress());
    }

    private void validateChoiceAnswer(ApplicationFormQuestion question, String value) {
        List<String> options = question.normalizedOptions();
        if (question.getType() == QuestionTypeEnum.SINGLE_CHOICE && !options.contains(value)) {
            throw new InvalidApplicationAnswersException(
                    "A resposta da pergunta '" + question.getPrompt() + "' não é uma opção válida.");
        }
        if (question.getType() == QuestionTypeEnum.MULTIPLE_CHOICE) {
            List<String> selected = Arrays.stream(value.split(";"))
                    .map(String::trim)
                    .filter(selection -> !selection.isEmpty())
                    .toList();
            if (selected.stream().anyMatch(selection -> !options.contains(selection))) {
                throw new InvalidApplicationAnswersException(
                        "A resposta da pergunta '" + question.getPrompt() + "' contém uma opção inválida.");
            }
            if (question.getMaxSelections() != null && selected.size() > question.getMaxSelections()) {
                throw new InvalidApplicationAnswersException(
                        "A pergunta '" + question.getPrompt() + "' permite no máximo "
                                + question.getMaxSelections() + " seleções.");
            }
        }
    }

    private void applyProfileAnswers(Candidate candidate, ResolvedAnswers resolved) {
        if (candidate.getTechnologies() == null) {
            candidate.setTechnologies(new ArrayList<>());
        }
        if (resolved.form() == null || resolved.answers().isEmpty()) {
            return;
        }
        Map<Long, FormAnswer> answersByQuestion = resolved.answers().stream()
                .collect(Collectors.toMap(FormAnswer::questionId, answer -> answer));
        Map<String, Technology> technologiesByName = new LinkedHashMap<>();
        Map<Long, Technology> candidateTechnologies = new LinkedHashMap<>();
        if (candidate.getTechnologies() != null) {
            candidate.getTechnologies().forEach(technology ->
                    candidateTechnologies.put(technology.getId(), technology));
        }

        for (ApplicationFormQuestion question : resolved.form().getQuestions()) {
            FormAnswer answer = answersByQuestion.get(question.getId());
            if (answer == null || answer.isEmpty()) {
                continue;
            }
            if (ApplicationFormQuestionKeys.FULL_NAME.equals(question.getQuestionKey())) {
                candidate.setName(answer.answer().trim());
            } else if (ApplicationFormQuestionKeys.AGE.equals(question.getQuestionKey())) {
                try {
                    candidate.setAge(Integer.valueOf(answer.answer().trim()));
                } catch (NumberFormatException ignored) {
                }
            } else if (ApplicationFormQuestionKeys.ROLE.equals(question.getQuestionKey())) {
                candidate.setRole(answer.answer().trim());
            } else if (ApplicationFormQuestionKeys.EMAIL.equals(question.getQuestionKey())) {
                candidate.setEmail(answer.answer().trim());
            } else if (ApplicationFormQuestionKeys.PHONE.equals(question.getQuestionKey())) {
                candidate.setPhone(answer.answer().trim());
            } else if (ApplicationFormQuestionKeys.LINKEDIN_URL.equals(question.getQuestionKey())) {
                candidate.setLinkedinUrl(answer.answer().trim());
            } else if (ApplicationFormQuestionKeys.EDUCATION_LEVEL.equals(question.getQuestionKey())) {
                try {
                    candidate.setEducationLevel(EducationLevelEnum.valueOf(answer.answer().trim()));
                } catch (IllegalArgumentException ignored) {
                }
            } else if (ApplicationFormQuestionKeys.ENGLISH_LEVEL.equals(question.getQuestionKey())) {
                try {
                    candidate.setEnglishLevel(EnglishLevelEnum.valueOf(answer.answer().trim()));
                } catch (IllegalArgumentException ignored) {
                }
            } else if (applyAddressAnswer(candidate, question.getQuestionKey(), answer.answer().trim())) {
                // endereço aplicado
            } else if (question.getTechnologyCategory() != null
                    || question.getParentTechnologyId() != null) {
                if (technologiesByName.isEmpty()) {
                    technologiesByName = technologyGateway.findAll().stream()
                            .collect(Collectors.toMap(
                                    technology -> technology.getName().toLowerCase(Locale.ROOT),
                                    technology -> technology,
                                    (first, second) -> first,
                                    LinkedHashMap::new));
                }
                for (String selected : answer.answer().split(";")) {
                    Technology technology = technologiesByName.get(selected.trim().toLowerCase(Locale.ROOT));
                    if (technology != null) {
                        candidateTechnologies.putIfAbsent(technology.getId(), technology);
                    }
                }
            }
        }

        candidate.setTechnologies(new ArrayList<>(candidateTechnologies.values()));
    }

    private boolean applyAddressAnswer(Candidate candidate, String questionKey, String value) {
        if (questionKey == null) {
            return false;
        }
        CandidateAddress address = candidate.getAddress();
        if (address == null) {
            address = new CandidateAddress();
            candidate.setAddress(address);
        }
        switch (questionKey) {
            case ApplicationFormQuestionKeys.STREET -> address.setStreet(value);
            case ApplicationFormQuestionKeys.ADDRESS_NUMBER -> address.setNumber(value);
            case ApplicationFormQuestionKeys.COMPLEMENT -> address.setComplement(value);
            case ApplicationFormQuestionKeys.NEIGHBORHOOD -> address.setNeighborhood(value);
            case ApplicationFormQuestionKeys.CITY -> address.setCity(value);
            case ApplicationFormQuestionKeys.STATE -> address.setState(value);
            case ApplicationFormQuestionKeys.ZIP_CODE -> address.setZipCode(value);
            case ApplicationFormQuestionKeys.COUNTRY -> address.setCountry(value);
            default -> {
                return false;
            }
        }
        return true;
    }

    private record ResolvedAnswers(ApplicationForm form, List<FormAnswer> answers) {
    }
}
