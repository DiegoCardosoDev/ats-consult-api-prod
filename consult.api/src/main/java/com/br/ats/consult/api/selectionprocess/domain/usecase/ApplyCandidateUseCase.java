package com.br.ats.consult.api.selectionprocess.domain.usecase;

import com.br.ats.consult.api.applicationform.domain.model.FormAnswerInput;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplicationResult;
import com.br.ats.consult.api.selectionprocess.domain.model.ResumeUpload;

import java.util.List;

public interface ApplyCandidateUseCase {

    CandidateApplicationResult applyToProcess(Long processId, Candidate candidate, ResumeUpload resume,
                                              List<FormAnswerInput> answers);

    CandidateApplicationResult applyToJob(Long jobId, Candidate candidate, ResumeUpload resume,
                                          List<FormAnswerInput> answers);
}
