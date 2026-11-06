package br.com.ats.consult.api.report.infrastructure.persistence.adapter;

import br.com.ats.consult.api.report.domain.port.out.ProcessDashboardGateway;
import br.com.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import br.com.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import br.com.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProcessDashboardGatewayAdapter implements ProcessDashboardGateway {

    private final SelectionProcessGateway selectionProcessGateway;

    public ProcessDashboardGatewayAdapter(SelectionProcessGateway selectionProcessGateway) {
        this.selectionProcessGateway = selectionProcessGateway;
    }

    @Override
    public Optional<SelectionProcess> findProcess(Long processId) {
        return selectionProcessGateway.findById(processId);
    }

    @Override
    public List<CandidateApplication> findApplications(Long processId) {
        return selectionProcessGateway.findApplicationsByProcessId(processId);
    }
}
