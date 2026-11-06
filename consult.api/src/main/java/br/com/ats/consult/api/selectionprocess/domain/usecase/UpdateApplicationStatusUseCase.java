package br.com.ats.consult.api.selectionprocess.domain.usecase;

import br.com.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import br.com.ats.consult.api.selectionprocess.domain.model.CandidateApplication;

public interface UpdateApplicationStatusUseCase {

    CandidateApplication execute(Long applicationId, CandidateApplicationStatusEnum status);
}
