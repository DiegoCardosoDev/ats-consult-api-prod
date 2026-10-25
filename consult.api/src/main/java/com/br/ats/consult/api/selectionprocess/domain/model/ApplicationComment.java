package com.br.ats.consult.api.selectionprocess.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationComment {

    private Long id;
    private Long applicationId;
    private Long authorId;
    private String authorEmail;
    private String message;
    private Instant createdAt;

    public void validate() {
        if (applicationId == null) {
            throw new IllegalArgumentException("A candidatura do comentário é obrigatória.");
        }
        if (authorEmail == null || authorEmail.isBlank()) {
            throw new IllegalArgumentException("O autor do comentário é obrigatório.");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("O comentário não pode estar vazio.");
        }
    }
}
