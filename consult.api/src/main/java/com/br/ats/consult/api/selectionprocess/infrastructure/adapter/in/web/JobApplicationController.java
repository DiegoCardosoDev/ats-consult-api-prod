package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web;

import com.br.ats.consult.api.applicationform.domain.model.FormAnswerInput;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto.CandidateRequest;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.mapper.CandidateWebMapper;
import com.br.ats.consult.api.selectionprocess.domain.exception.InvalidResumeException;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplicationResult;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;
import com.br.ats.consult.api.selectionprocess.domain.usecase.ApplyCandidateUseCase;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.ApplyCandidateResponse;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.FormAnswersRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/job-positions/{jobId}/applications")
public class JobApplicationController {

    private final ApplyCandidateUseCase applyCandidateUseCase;
    private final CandidateWebMapper candidateWebMapper;

    public JobApplicationController(ApplyCandidateUseCase applyCandidateUseCase,
                                    CandidateWebMapper candidateWebMapper) {
        this.applyCandidateUseCase = applyCandidateUseCase;
        this.candidateWebMapper = candidateWebMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApplyCandidateResponse> apply(
            @PathVariable Long jobId,
            @RequestParam Long clientId,
            @RequestPart("candidate") @Valid CandidateRequest candidateRequest,
            @RequestPart("resume") MultipartFile resume,
            @RequestPart(value = "answers", required = false) @Valid FormAnswersRequest answersRequest) {
        CandidateApplicationResult result = applyCandidateUseCase.applyToJob(
                jobId,
                clientId,
                candidateWebMapper.toDomain(candidateRequest),
                toResumeUpload(resume),
                toAnswerInputs(answersRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApplyCandidateResponse.fromDomain(result));
    }

    private List<FormAnswerInput> toAnswerInputs(FormAnswersRequest request) {
        if (request == null || request.answers() == null) {
            return List.of();
        }
        return request.answers().stream()
                .map(answer -> new FormAnswerInput(answer.questionId(), answer.answer()))
                .toList();
    }

    private ResumeUpload toResumeUpload(MultipartFile file) {
        try {
            return new ResumeUpload(file.getInputStream(), file.getOriginalFilename(),
                    file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new InvalidResumeException("Não foi possível ler o currículo enviado.");
        }
    }
}
