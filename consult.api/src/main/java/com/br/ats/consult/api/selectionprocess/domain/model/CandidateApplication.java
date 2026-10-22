package com.br.ats.consult.api.selectionprocess.domain.model;

import com.br.ats.consult.api.candidate.domain.model.Candidate;
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
public class CandidateApplication {

    private Long id;
    private Long selectionProcessId;
    private Candidate candidate;
    private ResumeMetadata resume;
    private Instant appliedAt;

    public void validate() {
        if (selectionProcessId == null) {
            throw new IllegalArgumentException("O processo seletivo da candidatura é obrigatório.");
        }
        if (candidate == null || candidate.getId() == null) {
            throw new IllegalArgumentException("O candidato da candidatura é obrigatório.");
        }
        if (resume == null || resume.getStorageKey() == null) {
            throw new IllegalArgumentException("O currículo da candidatura é obrigatório.");
        }
    }
}
