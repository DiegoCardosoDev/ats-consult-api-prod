package br.com.ats.consult.api.selectionprocess.infrastructure.persistence.repository.projection;

import br.com.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;

public interface ApplicationStatusCount {

    CandidateApplicationStatusEnum getStatus();

    Long getTotal();
}
