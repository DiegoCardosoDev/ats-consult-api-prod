package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository.projection;

import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;

public interface ProcessApplicationStatusCount {

    Long getProcessId();

    CandidateApplicationStatusEnum getStatus();

    Long getTotal();
}
