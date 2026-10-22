package com.br.ats.consult.api.selectionprocess.domain.model;

import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.selectionprocess.domain.enums.SelectionProcessStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectionProcess {

    private Long id;
    private Client client;
    private JobPosition jobPosition;
    private SelectionProcessStatusEnum status;
    private LocalDate openedAt;

    public void validate() {
        if (client == null || client.getId() == null) {
            throw new IllegalArgumentException("O cliente do processo seletivo é obrigatório.");
        }
        if (jobPosition == null || jobPosition.getId() == null) {
            throw new IllegalArgumentException("A vaga do processo seletivo é obrigatória.");
        }
        if (status == null) {
            throw new IllegalArgumentException("O estado do processo seletivo é obrigatório.");
        }
        if (openedAt == null) {
            throw new IllegalArgumentException("A data de abertura do processo seletivo é obrigatória.");
        }
    }

    public boolean isOpen() {
        return status == SelectionProcessStatusEnum.OPEN;
    }
}
