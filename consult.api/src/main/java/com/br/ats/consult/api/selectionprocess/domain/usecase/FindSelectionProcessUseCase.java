package com.br.ats.consult.api.selectionprocess.domain.usecase;

import com.br.ats.consult.api.selectionprocess.domain.model.CandidateApplication;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FindSelectionProcessUseCase {

    Optional<SelectionProcess> findById(Long id);

    Page<SelectionProcess> findAll(Pageable pageable);

    List<CandidateApplication> findApplications(Long processId);

    CandidateApplication findApplication(Long applicationId);
}
