package com.br.ats.consult.api.selectionprocess.domain.usecase.impl;

import com.br.ats.consult.api.selectionprocess.domain.model.ApplicationComment;
import com.br.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import com.br.ats.consult.api.selectionprocess.domain.usecase.ManageApplicationCommentUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public class ManageApplicationCommentUseCaseImpl implements ManageApplicationCommentUseCase {

    private final SelectionProcessGateway gateway;

    public ManageApplicationCommentUseCaseImpl(SelectionProcessGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public ApplicationComment addComment(Long applicationId, Long authorId, String authorEmail, String message) {
        ApplicationComment comment = ApplicationComment.builder()
                .applicationId(applicationId)
                .authorId(authorId)
                .authorEmail(authorEmail)
                .message(message == null ? null : message.trim())
                .createdAt(Instant.now())
                .build();
        comment.validate();
        return gateway.addComment(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationComment> listComments(Long applicationId) {
        return gateway.findCommentsByApplicationId(applicationId).stream()
                .sorted(Comparator.comparing(ApplicationComment::getCreatedAt).reversed()
                        .thenComparing(ApplicationComment::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
}
