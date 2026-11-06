package br.com.ats.consult.api.job.infrastructure.adapter.in.web;

import br.com.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import br.com.ats.consult.api.job.domain.model.JobPosition;
import br.com.ats.consult.api.job.domain.usecase.ChangeJobPositionStatusUseCase;
import br.com.ats.consult.api.job.domain.usecase.CreateJobPositionUseCase;
import br.com.ats.consult.api.job.domain.usecase.DeleteJobPositionUseCase;
import br.com.ats.consult.api.job.domain.usecase.FindJobPositionUseCase;
import br.com.ats.consult.api.job.domain.usecase.UpdateJobPositionUseCase;
import br.com.ats.consult.api.job.infrastructure.adapter.in.web.dto.JobPositionRequest;
import br.com.ats.consult.api.job.infrastructure.adapter.in.web.dto.JobPositionResponse;
import br.com.ats.consult.api.job.infrastructure.adapter.in.web.dto.JobStatusChangeRequest;
import br.com.ats.consult.api.job.infrastructure.adapter.in.web.dto.UpdateJobPositionRequest;
import br.com.ats.consult.api.job.infrastructure.adapter.in.web.mapper.JobPositionWebMapper;
import br.com.ats.consult.api.shared.infrastructure.adapter.in.web.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/job-positions")
public class JobPositionController {

    private final CreateJobPositionUseCase createJobPositionUseCase;
    private final FindJobPositionUseCase findJobPositionUseCase;
    private final UpdateJobPositionUseCase updateJobPositionUseCase;
    private final DeleteJobPositionUseCase deleteJobPositionUseCase;
    private final ChangeJobPositionStatusUseCase changeJobPositionStatusUseCase;
    private final JobPositionWebMapper webMapper;

    public JobPositionController(CreateJobPositionUseCase createJobPositionUseCase,
                                 FindJobPositionUseCase findJobPositionUseCase,
                                 UpdateJobPositionUseCase updateJobPositionUseCase,
                                 DeleteJobPositionUseCase deleteJobPositionUseCase,
                                 ChangeJobPositionStatusUseCase changeJobPositionStatusUseCase,
                                 JobPositionWebMapper webMapper) {
        this.createJobPositionUseCase = createJobPositionUseCase;
        this.findJobPositionUseCase = findJobPositionUseCase;
        this.updateJobPositionUseCase = updateJobPositionUseCase;
        this.deleteJobPositionUseCase = deleteJobPositionUseCase;
        this.changeJobPositionStatusUseCase = changeJobPositionStatusUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<JobPositionResponse> create(@RequestBody @Valid JobPositionRequest request) {
        JobPosition created = createJobPositionUseCase.execute(webMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPositionResponse> findById(@PathVariable Long id) {
        JobPosition domain = findJobPositionUseCase.findById(id)
                .orElseThrow(() -> new JobPositionNotFoundException(id));
        return ResponseEntity.ok(webMapper.toResponse(domain));
    }

    @GetMapping
    public ResponseEntity<PageResponse<JobPositionResponse>> findAll(
            @RequestParam(required = false) Long technologyId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<JobPosition> page = technologyId == null
                ? findJobPositionUseCase.findAll(pageable)
                : findJobPositionUseCase.findByTechnologyId(technologyId, pageable);

        return ResponseEntity.ok(PageResponse.from(page.map(webMapper::toResponse)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPositionResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateJobPositionRequest request) {
        JobPosition updated = updateJobPositionUseCase.execute(id, webMapper.toDomain(request));
        return ResponseEntity.ok(webMapper.toResponse(updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobPositionResponse> changeStatus(
            @PathVariable Long id,
            @RequestBody @Valid JobStatusChangeRequest request) {
        JobPosition updated = changeJobPositionStatusUseCase.execute(id, request.status(), request.reason());
        return ResponseEntity.ok(webMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteJobPositionUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
