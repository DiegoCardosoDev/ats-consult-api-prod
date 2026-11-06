package br.com.ats.consult.api.selectionprocess.infrastructure.persistence.repository.projection;

import br.com.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;

public interface ProcessApplicationStatusCount {

    Long getProcessId();

    CandidateApplicationStatusEnum getStatus();

    Long getTotal();
}
