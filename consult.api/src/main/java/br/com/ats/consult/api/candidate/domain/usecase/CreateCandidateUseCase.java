package br.com.ats.consult.api.candidate.domain.usecase;

import br.com.ats.consult.api.candidate.domain.model.Candidate;

public interface CreateCandidateUseCase {

    Candidate execute(Candidate candidate);
}
