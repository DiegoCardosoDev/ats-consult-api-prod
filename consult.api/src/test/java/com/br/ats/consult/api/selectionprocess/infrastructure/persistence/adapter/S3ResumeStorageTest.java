package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.adapter;

import com.br.ats.consult.api.selectionprocess.domain.exception.InvalidResumeException;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeContent;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeMetadata;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ResumeStorageTest {

    private static final long MAX_SIZE = 1024 * 1024;

    @Mock
    private S3Client s3Client;

    private S3ResumeStorage storage;

    @BeforeEach
    void setUp() {
        storage = new S3ResumeStorage(s3Client, "resumes-bucket", "/resumes/", MAX_SIZE);
    }

    @Test
    void storesPdfWithPrefixAndMetadata() {
        byte[] pdf = pdf();

        ResumeMetadata metadata = storage.store(upload(pdf));

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        assertEquals("resumes-bucket", requestCaptor.getValue().bucket());
        assertTrue(requestCaptor.getValue().key().startsWith("resumes/"));
        assertTrue(requestCaptor.getValue().key().endsWith(".pdf"));
        assertEquals("application/pdf", requestCaptor.getValue().contentType());
        assertEquals(pdf.length, requestCaptor.getValue().contentLength());
        assertEquals("curriculo.pdf", metadata.getFilename());
        assertEquals(pdf.length, metadata.getSizeBytes());
        assertEquals(requestCaptor.getValue().key(), metadata.getStorageKey());
    }

    @Test
    void loadsPdfFromS3() {
        byte[] pdf = pdf();
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(
                        GetObjectResponse.builder().contentType("application/pdf").build(), pdf));

        ResumeContent content = storage.load("resumes/curriculo.pdf");

        assertArrayEquals(pdf, content.content());
        assertEquals("application/pdf", content.contentType());
    }

    @Test
    void rejectsBlankStorageKeyWhenLoading() {
        assertThrows(InvalidResumeException.class, () -> storage.load(" "));
    }

    @Test
    void ignoresBlankStorageKeyWhenDeleting() {
        storage.delete(" ");

        verifyNoInteractions(s3Client);
    }

    @Test
    void requiresBucketWhenS3IsEnabled() {
        assertThrows(IllegalStateException.class,
                () -> new S3ResumeStorage(s3Client, "", "resumes", MAX_SIZE));
    }

    @Test
    void rejectsPathTraversalInPrefix() {
        assertThrows(IllegalStateException.class,
                () -> new S3ResumeStorage(s3Client, "resumes-bucket", "../resumes", MAX_SIZE));
    }

    private ResumeUpload upload(byte[] content) {
        return new ResumeUpload(new ByteArrayInputStream(content), "curriculo.pdf",
                "application/pdf", content.length);
    }

    private byte[] pdf() {
        return "%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF\n"
                .getBytes(StandardCharsets.US_ASCII);
    }
}
