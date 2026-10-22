package com.br.ats.consult.api.selectionprocess.domain.usecase;

import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplicationResult;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;

public interface ApplyCandidateUseCase {

    CandidateApplicationResult applyToProcess(Long processId, Candidate candidate, ResumeUpload resume);

    CandidateApplicationResult applyToJob(Long jobId, Long clientId, Candidate candidate, ResumeUpload resume);
}
