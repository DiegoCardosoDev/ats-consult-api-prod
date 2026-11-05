package com.br.ats.consult.api.report.domain.usecase;

import com.br.ats.consult.api.report.domain.model.ProcessDashboard;

public interface GenerateProcessDashboardUseCase {
    ProcessDashboard execute(Long processId);
}
