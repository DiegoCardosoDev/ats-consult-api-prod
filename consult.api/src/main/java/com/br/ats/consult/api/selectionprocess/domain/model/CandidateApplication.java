package com.br.ats.consult.api.selectionprocess.domain.model;

import com.br.ats.consult.api.applicationform.domain.model.FormAnswer;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.exception.PrivacyConsentRequiredException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    @Builder.Default
    private CandidateApplicationStatusEnum status = CandidateApplicationStatusEnum.TRIAGEM;
    @Builder.Default
    private List<FormAnswer> answers = new ArrayList<>();
    @Builder.Default
    private List<ApplicationComment> comments = new ArrayList<>();
    @Builder.Default
    private boolean consentAccepted = false;
    private Instant consentAcceptedAt;
    private String consentVersion;
    private String consentIp;
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
        if (status == null) {
            throw new IllegalArgumentException("O estado da candidatura é obrigatório.");
        }
        if (!consentAccepted) {
            throw new PrivacyConsentRequiredException();
        }
        if (consentAcceptedAt == null) {
            throw new PrivacyConsentRequiredException(
                    "A data de aceitação da Política de Privacidade é obrigatória.");
        }
    }
}
