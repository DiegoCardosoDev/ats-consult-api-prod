package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.selectionprocess.domain.model.ApplicationComment;

import java.time.Instant;

public record ApplicationCommentResponse(
        Long id,
        Long applicationId,
        Long authorId,
        String authorEmail,
        String message,
        Instant createdAt
) {

    public static ApplicationCommentResponse fromDomain(ApplicationComment comment) {
        if (comment == null) {
            return null;
        }
        return new ApplicationCommentResponse(
                comment.getId(),
                comment.getApplicationId(),
                comment.getAuthorId(),
                comment.getAuthorEmail(),
                comment.getMessage(),
                comment.getCreatedAt()
        );
    }
}
