package br.com.ats.consult.api.selectionprocess.domain.model;

public record ResumeContent(
        byte[] content,
        String contentType,
        String filename
) {
}
