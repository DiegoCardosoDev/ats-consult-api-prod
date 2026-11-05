package com.br.ats.consult.api.job.domain.usecase.impl;

import com.br.ats.consult.api.job.domain.enums.JobStatusEnum;
import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.job.domain.usecase.ChangeJobPositionStatusUseCase;
import org.springframework.transaction.annotation.Transactional;

public class ChangeJobPositionStatusUseCaseImpl implements ChangeJobPositionStatusUseCase {

    private final JobPositionGateway gateway;

    public ChangeJobPositionStatusUseCaseImpl(JobPositionGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public JobPosition execute(Long id, JobStatusEnum status, String reason) {
        if (status == null) {
            throw new IllegalArgumentException("O estado da vaga é obrigatório.");
        }
        if (status != JobStatusEnum.OPEN && status != JobStatusEnum.FROZEN && status != JobStatusEnum.CLOSED) {
            throw new IllegalArgumentException(
                    "A alteração de estado suporta apenas abertura, congelamento ou fechamento da vaga.");
        }

        JobPosition job = gateway.findById(id)
                .orElseThrow(() -> new JobPositionNotFoundException(id));

        if (status == JobStatusEnum.CLOSED || status == JobStatusEnum.FROZEN) {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException(
                        "O motivo do fechamento ou congelamento da vaga é obrigatório.");
            }
            job.setStatusReason(reason.trim());
        } else {
            if (job.getStatus() != JobStatusEnum.CLOSED && job.getStatus() != JobStatusEnum.FROZEN) {
                throw new IllegalArgumentException("Apenas vagas fechadas ou congeladas podem ser reabertas.");
            }
            job.setStatusReason(null);
        }

        job.setStatus(status);
        job.validate();
        return gateway.update(id, job);
    }
}
