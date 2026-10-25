package com.br.ats.consult.api.selectionprocess.domain.usecase;

import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;

public interface UpdateApplicationStatusUseCase {

    CandidateApplication execute(Long applicationId, CandidateApplicationStatusEnum status);
}
