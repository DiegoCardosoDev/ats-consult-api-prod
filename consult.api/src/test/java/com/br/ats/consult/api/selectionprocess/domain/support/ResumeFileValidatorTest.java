package com.br.ats.consult.api.selectionprocess.domain.support;

import com.br.ats.consult.api.selectionprocess.domain.exception.InvalidResumeException;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;
import com.br.ats.consult.api.selectionprocess.domain.support.ResumeFileValidator.ValidatedResume;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResumeFileValidatorTest {

    private static final long MAX_SIZE = 1024 * 1024;

    @Test
    void acceptsValidPdfAndSanitizesFilename() {
        byte[] pdf = pdf();

        ValidatedResume result = ResumeFileValidator.validate(upload("../curriculo.pdf", pdf), MAX_SIZE);

        assertEquals("curriculo.pdf", result.filename());
        assertArrayEquals(pdf, result.content());
    }

    @Test
    void rejectsNonPdfExtension() {
        assertThrows(InvalidResumeException.class,
                () -> ResumeFileValidator.validate(upload("curriculo.txt", pdf()), MAX_SIZE));
    }

    @Test
    void rejectsInvalidPdfSignature() {
        assertThrows(InvalidResumeException.class,
                () -> ResumeFileValidator.validate(upload("curriculo.pdf", "not a pdf".getBytes()), MAX_SIZE));
    }

    @Test
    void rejectsOversizedPdf() {
        assertThrows(InvalidResumeException.class,
                () -> ResumeFileValidator.validate(upload("curriculo.pdf", pdf()), 10));
    }

    private ResumeUpload upload(String filename, byte[] content) {
        return new ResumeUpload(new ByteArrayInputStream(content), filename, "application/pdf", content.length);
    }

    private byte[] pdf() {
        return "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF\n"
                .getBytes(StandardCharsets.US_ASCII);
    }
}
