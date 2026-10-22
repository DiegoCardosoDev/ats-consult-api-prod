package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.adapter;

import com.br.ats.consult.api.selectionprocess.domain.exception.InvalidResumeException;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeContent;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeMetadata;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;
import com.br.ats.consult.api.selectionprocess.domain.port.out.ResumeStorage;
import com.br.ats.consult.api.selectionprocess.domain.support.ResumeFileValidator;
import com.br.ats.consult.api.selectionprocess.domain.support.ResumeFileValidator.ValidatedResume;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.uploads.storage", havingValue = "local", matchIfMissing = true)
public class LocalResumeStorage implements ResumeStorage {

    @Value("${app.uploads.resumes-dir:uploads/resumes}")
    private String resumesDirectory;

    @Value("${app.uploads.resume-max-size-bytes:10485760}")
    private long maxSizeBytes;

    private Path baseDirectory;

    @PostConstruct
    void initialize() {
        try {
            baseDirectory = Paths.get(resumesDirectory).toAbsolutePath().normalize();
            Files.createDirectories(baseDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível preparar o diretório de currículos.", ex);
        }
    }

    @Override
    public ResumeMetadata store(ResumeUpload upload) {
        ValidatedResume validated = ResumeFileValidator.validate(upload, maxSizeBytes);

        String storageKey = UUID.randomUUID() + ".pdf";
        Path target = resolveSafely(storageKey);
        try {
            Files.write(target, validated.content());
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível guardar o currículo.", ex);
        }

        return ResumeMetadata.builder()
                .filename(validated.filename())
                .contentType("application/pdf")
                .sizeBytes(validated.content().length)
                .storageKey(storageKey)
                .build();
    }

    @Override
    public ResumeContent load(String storageKey) {
        Path file = resolveSafely(storageKey);
        try {
            return new ResumeContent(Files.readAllBytes(file), "application/pdf", storageKey);
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível ler o currículo guardado.", ex);
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolveSafely(storageKey));
        } catch (IOException ignored) {
            // A limpeza de ficheiros é best-effort para não mascarar o erro original.
        }
    }

    private Path resolveSafely(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new InvalidResumeException("A referência do currículo é inválida.");
        }
        Path resolved = baseDirectory.resolve(storageKey).normalize();
        if (!resolved.startsWith(baseDirectory)) {
            throw new InvalidResumeException("A referência do currículo é inválida.");
        }
        return resolved;
    }
}
