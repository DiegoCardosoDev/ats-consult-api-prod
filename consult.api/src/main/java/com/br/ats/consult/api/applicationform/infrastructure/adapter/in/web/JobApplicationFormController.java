package com.br.ats.consult.api.applicationform.infrastructure.adapter.in.web;

import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationFormQuestion;
import com.br.ats.consult.api.applicationform.domain.usecase.ManageApplicationFormUseCase;
import com.br.ats.consult.api.applicationform.infrastructure.adapter.in.web.dto.ApplicationFormQuestionRequest;
import com.br.ats.consult.api.applicationform.infrastructure.adapter.in.web.dto.ApplicationFormRequest;
import com.br.ats.consult.api.applicationform.infrastructure.adapter.in.web.dto.ApplicationFormResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/job-positions/{jobId}/application-form")
public class JobApplicationFormController {

    private final ManageApplicationFormUseCase useCase;

    public JobApplicationFormController(ManageApplicationFormUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<ApplicationFormResponse> findByJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApplicationFormResponse.fromDomain(useCase.resolveForJob(jobId)));
    }

    @PutMapping
    public ResponseEntity<ApplicationFormResponse> save(@PathVariable Long jobId,
                                                        @RequestBody @Valid ApplicationFormRequest request) {
        ApplicationForm saved = useCase.save(toDomain(jobId, request));
        return ResponseEntity.ok(ApplicationFormResponse.fromDomain(saved));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApplicationFormResponse> generate(@PathVariable Long jobId) {
        ApplicationForm generated = useCase.generateForJob(jobId);
        return ResponseEntity.ok(ApplicationFormResponse.fromDomain(generated));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long jobId) {
        useCase.deleteByJobPositionId(jobId);
        return ResponseEntity.noContent().build();
    }

    private ApplicationForm toDomain(Long jobId, ApplicationFormRequest request) {
        List<ApplicationFormQuestion> questions = request.questions() == null
                ? new ArrayList<>()
                : request.questions().stream()
                .sorted(Comparator.comparing(question -> question.position() == null ? Integer.MAX_VALUE : question.position()))
                .map(this::toDomain)
                .toList();

        return ApplicationForm.builder()
                .jobPositionId(jobId)
                .title(request.title())
                .description(request.description() == null ? "" : request.description())
                .active(request.active())
                .questions(new ArrayList<>(questions))
                .build();
    }

    private ApplicationFormQuestion toDomain(ApplicationFormQuestionRequest question) {
        return ApplicationFormQuestion.builder()
                .prompt(question.prompt())
                .type(question.type())
                .required(question.required())
                .options(question.options() == null ? new ArrayList<>() : new ArrayList<>(question.options()))
                .position(question.position())
                .questionKey(question.questionKey())
                .technologyCategory(question.technologyCategory())
                .parentTechnologyId(question.parentTechnologyId())
                .maxSelections(question.maxSelections())
                .systemGenerated(Boolean.TRUE.equals(question.systemGenerated()))
                .build();
    }
}
