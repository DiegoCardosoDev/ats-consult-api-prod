package br.com.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.mapper;

import br.com.ats.consult.api.client.domain.model.Client;
import br.com.ats.consult.api.job.domain.model.JobPosition;
import br.com.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import br.com.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto.CreateSelectionProcessRequest;
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
