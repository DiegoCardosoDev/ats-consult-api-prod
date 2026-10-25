package com.br.ats.consult.api.candidate.infrastructure.adapter.in.web;

import com.br.ats.consult.api.candidate.domain.exception.CandidateNotFoundException;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.usecase.CreateCandidateUseCase;
import com.br.ats.consult.api.candidate.domain.usecase.FindCandidateUseCase;
import com.br.ats.consult.api.candidate.domain.usecase.UpdateCandidateUseCase;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto.CandidateRequest;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto.CandidateResponse;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.mapper.CandidateWebMapper;
import com.br.ats.consult.api.shared.infrastructure.adapter.in.web.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateController {

    private final CreateCandidateUseCase createCandidateUseCase;
    private final FindCandidateUseCase findCandidateUseCase;
    private final UpdateCandidateUseCase updateCandidateUseCase;
    private final CandidateWebMapper webMapper;

    public CandidateController(CreateCandidateUseCase createCandidateUseCase,
                               FindCandidateUseCase findCandidateUseCase,
                               UpdateCandidateUseCase updateCandidateUseCase,
                               CandidateWebMapper webMapper) {
        this.createCandidateUseCase = createCandidateUseCase;
        this.findCandidateUseCase = findCandidateUseCase;
        this.updateCandidateUseCase = updateCandidateUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<CandidateResponse> create(@RequestBody @Valid CandidateRequest request) {
        Candidate created = createCandidateUseCase.execute(webMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(CandidateResponse.fromDomain(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponse> update(@PathVariable Long id,
                                                    @RequestBody @Valid CandidateRequest request) {
        Candidate updated = updateCandidateUseCase.execute(id, webMapper.toDomain(request));
        return ResponseEntity.ok(CandidateResponse.fromDomain(updated));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CandidateResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Candidate> page = findCandidateUseCase.search(name, role, pageable);
        return ResponseEntity.ok(PageResponse.from(page.map(CandidateResponse::fromDomain)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponse> findById(@PathVariable Long id) {
        Candidate candidate = findCandidateUseCase.findById(id)
                .orElseThrow(() -> new CandidateNotFoundException(id));
        return ResponseEntity.ok(CandidateResponse.fromDomain(candidate));
    }
}
