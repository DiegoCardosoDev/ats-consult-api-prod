package com.br.ats.consult.api.candidate.domain.usecase;

import com.br.ats.consult.api.candidate.domain.model.Candidate;

public interface CreateCandidateUseCase {

    Candidate execute(Candidate candidate);
}
