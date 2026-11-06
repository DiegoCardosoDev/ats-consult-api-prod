package br.com.ats.consult.api.report.domain.port.out;

import br.com.ats.consult.api.report.domain.model.ProcessReportFilter;
import br.com.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import br.com.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProcessReportGateway {

    Page<SelectionProcess> findProcesses(ProcessReportFilter filter, Pageable pageable);

    long countProcesses(ProcessReportFilter filter);

    Map<Long, Map<CandidateApplicationStatusEnum, Long>> countApplicationsByProcessIds(List<Long> processIds);

    Map<CandidateApplicationStatusEnum, Long> countApplications(ProcessReportFilter filter);
}
