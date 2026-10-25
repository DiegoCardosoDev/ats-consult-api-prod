package com.br.ats.consult.api.selectionprocess.domain.usecase;

import com.br.ats.consult.api.selectionprocess.domain.model.ApplicationComment;

import java.util.List;

public interface ManageApplicationCommentUseCase {

    ApplicationComment addComment(Long applicationId, Long authorId, String authorEmail, String message);

    List<ApplicationComment> listComments(Long applicationId);
}
