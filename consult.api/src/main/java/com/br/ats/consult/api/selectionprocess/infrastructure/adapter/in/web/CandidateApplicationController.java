package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web;

import com.br.ats.consult.api.selectionprocess.domain.model.ApplicationComment;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeContent;
import com.br.ats.consult.api.selectionprocess.domain.port.out.ResumeStorage;
import com.br.ats.consult.api.selectionprocess.domain.usecase.FindSelectionProcessUseCase;
import com.br.ats.consult.api.selectionprocess.domain.usecase.ManageApplicationCommentUseCase;
import com.br.ats.consult.api.selectionprocess.domain.usecase.UpdateApplicationStatusUseCase;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.ApplicationCommentResponse;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.CandidateApplicationResponse;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.CreateApplicationCommentRequest;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.UpdateApplicationStatusRequest;
import com.br.ats.consult.api.security.model.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class CandidateApplicationController {

    private final FindSelectionProcessUseCase findSelectionProcessUseCase;
    private final UpdateApplicationStatusUseCase updateApplicationStatusUseCase;
    private final ManageApplicationCommentUseCase manageApplicationCommentUseCase;
    private final ResumeStorage resumeStorage;

    public CandidateApplicationController(FindSelectionProcessUseCase findSelectionProcessUseCase,
                                          UpdateApplicationStatusUseCase updateApplicationStatusUseCase,
                                          ManageApplicationCommentUseCase manageApplicationCommentUseCase,
                                          ResumeStorage resumeStorage) {
        this.findSelectionProcessUseCase = findSelectionProcessUseCase;
        this.updateApplicationStatusUseCase = updateApplicationStatusUseCase;
        this.manageApplicationCommentUseCase = manageApplicationCommentUseCase;
        this.resumeStorage = resumeStorage;
    }

    @GetMapping("/{id}/resume")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) {
        CandidateApplication application = findSelectionProcessUseCase.findApplication(id);
        ResumeContent content = resumeStorage.load(application.getResume().getStorageKey());

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(application.getResume().getFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(content.content());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CandidateApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateApplicationStatusRequest request) {
        CandidateApplication updated = updateApplicationStatusUseCase.execute(id, request.status());
        return ResponseEntity.ok(CandidateApplicationResponse.fromDomain(updated));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<ApplicationCommentResponse>> listComments(@PathVariable Long id) {
        List<ApplicationCommentResponse> comments = manageApplicationCommentUseCase.listComments(id).stream()
                .map(ApplicationCommentResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApplicationCommentResponse> addComment(
            @PathVariable Long id,
            @RequestBody @Valid CreateApplicationCommentRequest request,
            Authentication authentication) {
        Author author = resolveAuthor(authentication);
        ApplicationComment comment = manageApplicationCommentUseCase.addComment(
                id, author.id(), author.email(), request.message());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApplicationCommentResponse.fromDomain(comment));
    }

    private Author resolveAuthor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("É necessário estar autenticado para comentar.");
        }
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return new Author(principal.getId(), principal.getUsername());
        }
        return new Author(null, authentication.getName());
    }

    private record Author(Long id, String email) {
    }
}
