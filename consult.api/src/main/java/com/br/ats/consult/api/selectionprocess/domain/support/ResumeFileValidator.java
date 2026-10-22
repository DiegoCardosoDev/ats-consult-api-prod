package com.br.ats.consult.api.selectionprocess.domain.support;

import com.br.ats.consult.api.selectionprocess.domain.exception.InvalidResumeException;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

public final class ResumeFileValidator {

    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private ResumeFileValidator() {
    }

    public static ValidatedResume validate(ResumeUpload upload, long maxSizeBytes) {
        if (upload == null || upload.content() == null) {
            throw new InvalidResumeException("O currículo em PDF é obrigatório.");
        }
        if (upload.size() <= 0) {
            throw new InvalidResumeException("O ficheiro do currículo está vazio.");
        }
        if (upload.size() > maxSizeBytes) {
            throw new InvalidResumeException("O currículo excede o tamanho máximo permitido.");
        }

        String originalFilename = sanitizeFilename(upload.originalFilename());
        if (!originalFilename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new InvalidResumeException("O currículo deve estar no formato PDF.");
        }

        byte[] content;
        try (InputStream input = upload.content()) {
            content = input.readAllBytes();
        } catch (IOException ex) {
            throw new InvalidResumeException("Não foi possível ler o currículo enviado.");
        }

        if (content.length == 0) {
            throw new InvalidResumeException("O ficheiro do currículo está vazio.");
        }
        if (content.length > maxSizeBytes) {
            throw new InvalidResumeException("O currículo excede o tamanho máximo permitido.");
        }
        if (!startsWithPdfSignature(content)) {
            throw new InvalidResumeException("O ficheiro enviado não é um PDF válido.");
        }

        return new ValidatedResume(originalFilename, content);
    }

    private static boolean startsWithPdfSignature(byte[] content) {
        if (content.length < PDF_SIGNATURE.length) {
            return false;
        }
        return Arrays.equals(Arrays.copyOf(content, PDF_SIGNATURE.length), PDF_SIGNATURE);
    }

    private static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new InvalidResumeException("O nome do ficheiro do currículo é obrigatório.");
        }
        try {
            Path path = Path.of(filename);
            Path fileName = path.getFileName();
            String baseName = (fileName == null ? "" : fileName.toString())
                    .replaceAll("[\\r\\n\\t]", "_")
                    .trim();
            if (baseName.isEmpty()) {
                throw new InvalidResumeException("O nome do ficheiro do currículo é inválido.");
            }
            return baseName.length() > 255 ? baseName.substring(baseName.length() - 255) : baseName;
        } catch (InvalidPathException ex) {
            throw new InvalidResumeException("O nome do ficheiro do currículo é inválido.");
        }
    }

    public record ValidatedResume(String filename, byte[] content) {
    }
}
