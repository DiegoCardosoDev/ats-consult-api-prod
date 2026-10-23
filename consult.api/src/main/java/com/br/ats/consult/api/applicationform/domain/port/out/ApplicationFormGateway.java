package com.br.ats.consult.api.applicationform.domain.port.out;

import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;

import java.util.Optional;

public interface ApplicationFormGateway {

    Optional<ApplicationForm> findByJobPositionId(Long jobPositionId);

    ApplicationForm save(ApplicationForm form);

    void deleteByJobPositionId(Long jobPositionId);

    boolean existsByJobPositionId(Long jobPositionId);
}
