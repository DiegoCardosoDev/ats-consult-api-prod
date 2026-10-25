package com.br.ats.consult.api.applicationform.domain.usecase;

import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;

import java.util.Optional;

public interface ManageApplicationFormUseCase {

    Optional<ApplicationForm> findByJobPositionId(Long jobPositionId);

    ApplicationForm resolveForJob(Long jobPositionId);

    ApplicationForm save(ApplicationForm form);

    ApplicationForm generateForJob(Long jobPositionId);

    void deleteByJobPositionId(Long jobPositionId);
}
