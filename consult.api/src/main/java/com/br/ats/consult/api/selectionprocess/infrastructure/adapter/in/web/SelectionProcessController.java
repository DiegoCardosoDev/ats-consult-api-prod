package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web;

import com.br.ats.consult.api.applicationform.domain.model.FormAnswerInput;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto.CandidateRequest;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.mapper.CandidateWebMapper;
import com.br.ats.consult.api.selectionprocess.domain.exception.InvalidResumeException;
import com.br.ats.consult.api.selectionprocess.domain.exception.SelectionProcessNotFoundException;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplicationResult;
import com.br.ats.consult.api.selectionprocess.domain.model.PrivacyConsentInput;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.domain.usecase.ApplyCandidateUseCase;
import com.br.ats.consult.api.selectionprocess.domain.usecase.CreateSelectionProcessUseCase;
import com.br.ats.consult.api.selectionprocess.domain.usecase.FindSelectionProcessUseCase;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.ApplyCandidateResponse;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.CreateSelectionProcessRequest;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.FormAnswersRequest;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.PrivacyConsentRequest;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.SelectionProcessResponse;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.SelectionProcessSummaryResponse;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.mapper.SelectionProcessWebMapper;
import com.br.ats.consult.api.shared.infrastructure.adapter.in.web.dto.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/selection-processes")
public class SelectionProcessController {

    private final CreateSelectionProcessUseCase createSelectionProcessUseCase;
    private final FindSelectionProcessUseCase findSelectionProcessUseCase;
    private final ApplyCandidateUseCase applyCandidateUseCase;
    private final CandidateWebMapper candidateWebMapper;
    private final SelectionProcessWebMapper webMapper;

    public SelectionProcessController(CreateSelectionProcessUseCase createSelectionProcessUseCase,
                                      FindSelectionProcessUseCase findSelectionProcessUseCase,
                                      ApplyCandidateUseCase applyCandidateUseCase,
                                      CandidateWebMapper candidateWebMapper,
                                      SelectionProcessWebMapper webMapper) {
        this.createSelectionProcessUseCase = createSelectionProcessUseCase;
        this.findSelectionProcessUseCase = findSelectionProcessUseCase;
        this.applyCandidateUseCase = applyCandidateUseCase;
        this.candidateWebMapper = candidateWebMapper;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<SelectionProcessResponse> create(
            @RequestBody @Valid CreateSelectionProcessRequest request) {
        SelectionProcess created = createSelectionProcessUseCase.execute(webMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SelectionProcessResponse.fromDomain(created, List.of()));
    }

    @GetMapping
    public ResponseEntity<PageResponse<SelectionProcessSummaryResponse>> findAll(
            @PageableDefault(size = 20, sort = "openedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SelectionProcess> page = findSelectionProcessUseCase.findAll(pageable);
        return ResponseEntity.ok(PageResponse.from(page.map(SelectionProcessSummaryResponse::fromDomain)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SelectionProcessResponse> findById(@PathVariable Long id) {
        SelectionProcess process = findSelectionProcessUseCase.findById(id)
                .orElseThrow(() -> new SelectionProcessNotFoundException(id));
        List<CandidateApplication> applications = findSelectionProcessUseCase.findApplications(id);
        return ResponseEntity.ok(SelectionProcessResponse.fromDomain(process, applications));
    }

    @PostMapping(value = "/{id}/candidates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApplyCandidateResponse> addCandidate(
            @PathVariable Long id,
            @RequestPart("candidate") @Valid CandidateRequest candidateRequest,
            @RequestPart("resume") MultipartFile resume,
            @RequestPart(value = "answers", required = false) @Valid FormAnswersRequest answersRequest,
            @RequestPart(value = "consent", required = false) @Valid PrivacyConsentRequest consentRequest,
            HttpServletRequest httpRequest) {
        CandidateApplicationResult result = applyCandidateUseCase.applyToProcess(
                id,
                candidateWebMapper.toDomain(candidateRequest),
                toResumeUpload(resume),
                toAnswerInputs(answersRequest),
                toConsentInput(consentRequest, httpRequest)
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

    private PrivacyConsentInput toConsentInput(PrivacyConsentRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            return null;
        }
        return new PrivacyConsentInput(
                Boolean.TRUE.equals(request.accepted()),
                request.version(),
                httpRequest.getRemoteAddr()
        );
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
