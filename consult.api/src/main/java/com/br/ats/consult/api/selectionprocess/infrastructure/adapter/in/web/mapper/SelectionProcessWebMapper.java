package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.mapper;

import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.CreateSelectionProcessRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SelectionProcessWebMapper {

    default SelectionProcess toDomain(CreateSelectionProcessRequest request) {
        return SelectionProcess.builder()
                .client(Client.builder().id(request.clientId()).build())
                .jobPosition(JobPosition.builder().id(request.jobPositionId()).build())
                .status(request.status())
                .openedAt(request.openedAt())
                .build();
    }
}
