package com.br.ats.consult.api.selectionprocess.domain.port.out;

import com.br.ats.consult.api.selectionprocess.domain.enums.CandidateApplicationStatusEnum;
import com.br.ats.consult.api.selectionprocess.domain.model.ApplicationComment;
import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SelectionProcessGateway {

    SelectionProcess save(SelectionProcess process);

    Optional<SelectionProcess> findById(Long id);

    Page<SelectionProcess> findAll(Pageable pageable);

    Optional<SelectionProcess> findOpenByClientIdAndJobPositionId(Long clientId, Long jobPositionId);

    CandidateApplication addApplication(CandidateApplication application);

    List<CandidateApplication> findApplicationsByProcessId(Long processId);

    Optional<CandidateApplication> findApplicationById(Long applicationId);

    CandidateApplication updateApplicationStatus(Long applicationId, CandidateApplicationStatusEnum status);

    ApplicationComment addComment(ApplicationComment comment);

    List<ApplicationComment> findCommentsByApplicationId(Long applicationId);

    boolean existsApplication(Long processId, Long candidateId);
}
