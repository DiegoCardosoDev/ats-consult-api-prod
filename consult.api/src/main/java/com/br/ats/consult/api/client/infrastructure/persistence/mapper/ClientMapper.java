package com.br.ats.consult.api.client.infrastructure.persistence.mapper;

import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.client.infrastructure.persistence.entity.ClientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    Client toDomain(ClientEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    ClientEntity toEntity(Client domain);
}
