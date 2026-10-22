package com.br.ats.consult.api.selectionprocess.infrastructure.persistence.mapper;

import com.br.ats.consult.api.client.infrastructure.persistence.mapper.ClientMapper;
import com.br.ats.consult.api.job.infrastructure.persistence.mapper.JobPositionMapper;
import com.br.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import com.br.ats.consult.api.selectionprocess.infrastructure.persistence.entity.SelectionProcessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ClientMapper.class, JobPositionMapper.class})
public interface SelectionProcessMapper {

    SelectionProcess toDomain(SelectionProcessEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SelectionProcessEntity toEntity(SelectionProcess domain);
}
