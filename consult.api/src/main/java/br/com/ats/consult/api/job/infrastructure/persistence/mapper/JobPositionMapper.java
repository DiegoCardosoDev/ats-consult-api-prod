package br.com.ats.consult.api.job.infrastructure.persistence.mapper;

import br.com.ats.consult.api.client.infrastructure.persistence.mapper.ClientMapper;
import br.com.ats.consult.api.job.domain.model.JobPosition;
import br.com.ats.consult.api.job.infrastructure.persistence.entity.JobPositionEntity;
import br.com.ats.consult.api.technology.domain.model.Technology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ClientMapper.class})
public interface JobPositionMapper {

    @Mapping(target = "technologies", ignore = true)
    JobPosition toDomain(JobPositionEntity entity);

    @Mapping(target = "client", ignore = true)
    @Mapping(target = "technologyIds", expression = "java(mapTechnologiesToEntityIds(domain.getTechnologies()))")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    JobPositionEntity toEntity(JobPosition domain);

    default List<Long> mapTechnologiesToEntityIds(List<Technology> technologies) {
        if (technologies == null) {
            return List.of();
        }
        return technologies.stream()
                .map(Technology::getId)
                .toList();
    }
}
