package com.br.ats.consult.api.report.infrastructure.persistence.adapter;

import com.br.ats.consult.api.report.domain.model.ProcessReportFilter;
import com.br.ats.consult.api.report.domain.port.out.ProcessReportGateway;
import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.mapper.SelectionProcessMapper;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository.SpringDataCandidateApplicationRepository;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository.SpringDataSelectionProcessRepository;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository.projection.ApplicationStatusCount;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.repository.projection.ProcessApplicationStatusCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProcessReportRepositoryAdapter implements ProcessReportGateway {

    private final SpringDataSelectionProcessRepository processRepository;
    private final SpringDataCandidateApplicationRepository applicationRepository;
    private final SelectionProcessMapper processMapper;

    public ProcessReportRepositoryAdapter(SpringDataSelectionProcessRepository processRepository,
                                          SpringDataCandidateApplicationRepository applicationRepository,
                                          SelectionProcessMapper processMapper) {
        this.processRepository = processRepository;
        this.applicationRepository = applicationRepository;
        this.processMapper = processMapper;
    }

    @Override
    public Page<SelectionProcess> findProcesses(ProcessReportFilter filter, Pageable pageable) {
        return processRepository.findForReport(filter.clientId(), filter.jobPositionId(), filter.status(),
                        pageable)
                .map(processMapper::toDomain);
    }

    @Override
    public long countProcesses(ProcessReportFilter filter) {
        return processRepository.countForReport(filter.clientId(), filter.jobPositionId(), filter.status());
    }

    @Override
    public Map<Long, Map<CandidateApplicationStatusEnum, Long>> countApplicationsByProcessIds(
            List<Long> processIds) {
        if (processIds == null || processIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Map<CandidateApplicationStatusEnum, Long>> counts = new HashMap<>();
        for (ProcessApplicationStatusCount row : applicationRepository.countByProcessIds(processIds)) {
            counts.computeIfAbsent(row.getProcessId(), key -> new HashMap<>())
                    .put(row.getStatus(), row.getTotal());
        }
        return counts;
    }

    @Override
    public Map<CandidateApplicationStatusEnum, Long> countApplications(ProcessReportFilter filter) {
        return applicationRepository
                .countByReportFilter(filter.clientId(), filter.jobPositionId(), filter.status())
                .stream()
                .collect(Collectors.toMap(ApplicationStatusCount::getStatus, ApplicationStatusCount::getTotal));
    }
}
