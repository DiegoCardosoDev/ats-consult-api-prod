package com.br.ats.consult.api.selectionprocess.domain.model;

import java.io.InputStream;

public record ResumeUpload(
        InputStream content,
        String originalFilename,
        String contentType,
        long size
) {
}
