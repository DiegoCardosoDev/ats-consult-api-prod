package com.br.ats.consult.api.report.domain.port.out;

import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;

import java.util.List;
import java.util.Optional;

public interface ProcessDashboardGateway {

    Optional<SelectionProcess> findProcess(Long processId);

    List<CandidateApplication> findApplications(Long processId);
}
