package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.adapter;

import com.br.ats.consult.api.selectionprocess.domain.exception.InvalidResumeException;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeContent;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeMetadata;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;
import com.br.ats.consult.api.selectionprocess.domain.port.out.ResumeStorage;
import com.br.ats.consult.api.selectionprocess.domain.support.ResumeFileValidator;
import com.br.ats.consult.api.selectionprocess.domain.support.ResumeFileValidator.ValidatedResume;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.uploads.storage", havingValue = "s3")
public class S3ResumeStorage implements ResumeStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String pathPrefix;
    private final long maxSizeBytes;

    public S3ResumeStorage(S3Client s3Client,
                           @Value("${app.uploads.s3.bucket:}") String bucket,
                           @Value("${app.uploads.s3.path-prefix:resumes}") String pathPrefix,
                           @Value("${app.uploads.resume-max-size-bytes:10485760}") long maxSizeBytes) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("app.uploads.s3.bucket é obrigatório quando app.uploads.storage=s3.");
        }
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.pathPrefix = normalizePrefix(pathPrefix);
        this.maxSizeBytes = maxSizeBytes;
    }

    @Override
    public ResumeMetadata store(ResumeUpload upload) {
        ValidatedResume validated = ResumeFileValidator.validate(upload, maxSizeBytes);
        String storageKey = pathPrefix + UUID.randomUUID() + ".pdf";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .contentType("application/pdf")
                .contentLength((long) validated.content().length)
                .build();

        try {
            s3Client.putObject(request, RequestBody.fromBytes(validated.content()));
        } catch (S3Exception ex) {
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
        if (storageKey == null || storageKey.isBlank()) {
            throw new InvalidResumeException("A referência do currículo é inválida.");
        }

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build();

        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
            String contentType = response.response().contentType();
            return new ResumeContent(response.asByteArray(),
                    contentType == null || contentType.isBlank() ? "application/pdf" : contentType,
                    storageKey);
        } catch (NoSuchKeyException ex) {
            throw new IllegalStateException("Não foi possível ler o currículo guardado.", ex);
        } catch (S3Exception ex) {
            throw new IllegalStateException("Não foi possível ler o currículo guardado.", ex);
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(storageKey)
                    .build());
        } catch (S3Exception ignored) {
            // A limpeza de objetos é best-effort para não mascarar o erro original.
        }
    }

    private static String normalizePrefix(String pathPrefix) {
        if (pathPrefix == null || pathPrefix.isBlank()) {
            return "";
        }
        String normalized = pathPrefix.trim()
                .replace('\\', '/')
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
        if (normalized.contains("..")) {
            throw new IllegalStateException("app.uploads.s3.path-prefix não pode conter '..'.");
        }
        return normalized.isEmpty() ? "" : normalized + "/";
    }
}
