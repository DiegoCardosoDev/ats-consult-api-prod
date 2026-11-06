package br.com.ats.consult.api.selectionprocess.domain.usecase;

import br.com.ats.consult.api.applicationform.domain.model.FormAnswerInput;
import br.com.ats.consult.api.candidate.domain.model.Candidate;
import br.com.ats.consult.api.selectionprocess.domain.model.CandidateApplicationResult;
import br.com.ats.consult.api.selectionprocess.domain.model.PrivacyConsentInput;
import br.com.ats.consult.api.selectionprocess.domain.model.ResumeUpload;

import java.util.List;

public interface ApplyCandidateUseCase {

    CandidateApplicationResult applyToProcess(Long processId, Candidate candidate, ResumeUpload resume,
                                              List<FormAnswerInput> answers, PrivacyConsentInput consent);

    CandidateApplicationResult applyToJob(Long jobId, Candidate candidate, ResumeUpload resume,
                                          List<FormAnswerInput> answers, PrivacyConsentInput consent);
}
