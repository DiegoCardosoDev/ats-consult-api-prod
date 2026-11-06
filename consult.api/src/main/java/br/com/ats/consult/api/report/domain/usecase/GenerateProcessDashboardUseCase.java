package br.com.ats.consult.api.report.domain.usecase;

import br.com.ats.consult.api.report.domain.model.ProcessDashboard;

public interface GenerateProcessDashboardUseCase {
    ProcessDashboard execute(Long processId);
}
