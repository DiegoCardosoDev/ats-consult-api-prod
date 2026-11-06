package br.com.ats.consult.api.technology.infrastructure.persistence.mapper;

import br.com.ats.consult.api.technology.domain.model.Technology;
import br.com.ats.consult.api.technology.infrastructure.persistence.entity.TechnologyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TechnologyMapper {

    Technology toDomain(TechnologyEntity entity);

    TechnologyEntity toEntity(Technology domain);
}