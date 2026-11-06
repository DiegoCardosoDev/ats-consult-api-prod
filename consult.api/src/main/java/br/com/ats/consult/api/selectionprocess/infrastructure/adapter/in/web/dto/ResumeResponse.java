package br.com.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import br.com.ats.consult.api.selectionprocess.domain.model.ResumeMetadata;

public record ResumeResponse(
        String filename,
        String contentType,
        long sizeBytes
) {

    public static ResumeResponse fromDomain(ResumeMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        return new ResumeResponse(metadata.getFilename(), metadata.getContentType(), metadata.getSizeBytes());
    }
}
