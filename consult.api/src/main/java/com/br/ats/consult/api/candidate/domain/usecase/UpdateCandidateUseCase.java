package com.br.ats.consult.api.candidate.domain.usecase;

import com.br.ats.consult.api.candidate.domain.model.Candidate;

public interface UpdateCandidateUseCase {

    Candidate execute(Long id, Candidate candidate);
}
