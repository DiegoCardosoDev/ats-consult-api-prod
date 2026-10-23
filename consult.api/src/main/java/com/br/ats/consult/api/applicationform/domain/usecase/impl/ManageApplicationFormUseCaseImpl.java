package com.br.ats.consult.api.applicationform.domain.usecase.impl;

import com.br.ats.consult.api.applicationform.domain.exception.ApplicationFormNotFoundException;
import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;
import com.br.ats.consult.api.applicationform.domain.port.out.ApplicationFormGateway;
import com.br.ats.consult.api.applicationform.domain.usecase.ManageApplicationFormUseCase;
import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class ManageApplicationFormUseCaseImpl implements ManageApplicationFormUseCase {

    private final ApplicationFormGateway formGateway;
    private final JobPositionGateway jobPositionGateway;

    public ManageApplicationFormUseCaseImpl(ApplicationFormGateway formGateway,
                                            JobPositionGateway jobPositionGateway) {
        this.formGateway = formGateway;
        this.jobPositionGateway = jobPositionGateway;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationForm> findByJobPositionId(Long jobPositionId) {
        ensureJobExists(jobPositionId);
        return formGateway.findByJobPositionId(jobPositionId);
    }

    @Override
    @Transactional
    public ApplicationForm save(ApplicationForm form) {
        ensureJobExists(form.getJobPositionId());
        form.validate();

        formGateway.findByJobPositionId(form.getJobPositionId())
                .ifPresent(existing -> form.setId(existing.getId()));

        return formGateway.save(form);
    }

    @Override
    @Transactional
    public void deleteByJobPositionId(Long jobPositionId) {
        ensureJobExists(jobPositionId);
        if (!formGateway.existsByJobPositionId(jobPositionId)) {
            throw new ApplicationFormNotFoundException(jobPositionId);
        }
        formGateway.deleteByJobPositionId(jobPositionId);
    }

    private void ensureJobExists(Long jobPositionId) {
        if (jobPositionId == null || jobPositionGateway.findById(jobPositionId).isEmpty()) {
            throw new JobPositionNotFoundException(jobPositionId);
        }
    }
}
