package com.br.ats.consult.api.client.infrastructure.adapter.in.web.mapper;

import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.client.infrastructure.adapter.in.web.dto.CreateClientRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientWebMapper {

    @Mapping(target = "id", ignore = true)
    Client toDomain(CreateClientRequest request);
}
