package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web;

import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeContent;
import com.br.ats.consult.api.selectionprocess.domain.port.out.ResumeStorage;
import com.br.ats.consult.api.selectionprocess.domain.usecase.FindSelectionProcessUseCase;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/applications")
public class CandidateApplicationController {

    private final FindSelectionProcessUseCase findSelectionProcessUseCase;
    private final ResumeStorage resumeStorage;

    public CandidateApplicationController(FindSelectionProcessUseCase findSelectionProcessUseCase,
                                          ResumeStorage resumeStorage) {
        this.findSelectionProcessUseCase = findSelectionProcessUseCase;
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
}
