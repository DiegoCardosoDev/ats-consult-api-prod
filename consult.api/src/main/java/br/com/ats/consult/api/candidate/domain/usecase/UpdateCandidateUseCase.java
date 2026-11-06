package br.com.ats.consult.api.candidate.domain.usecase;

import br.com.ats.consult.api.candidate.domain.model.Candidate;

public interface UpdateCandidateUseCase {

    Candidate execute(Long id, Candidate candidate);
}
