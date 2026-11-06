package br.com.ats.consult.api.selectionprocess.infrastructure.persistence.mapper;

import br.com.ats.consult.api.client.infrastructure.persistence.mapper.ClientMapper;
import br.com.ats.consult.api.job.infrastructure.persistence.mapper.JobPositionMapper;
import br.com.ats.consult.api.selectionprocess.domain.model.SelectionProcess;
import br.com.ats.consult.api.selectionprocess.infrastructure.persistence.entity.SelectionProcessEntity;
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
