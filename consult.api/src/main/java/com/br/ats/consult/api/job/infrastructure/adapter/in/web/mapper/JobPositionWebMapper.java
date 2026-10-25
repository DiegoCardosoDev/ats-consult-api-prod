package com.br.ats.consult.api.job.infrastructure.adapter.in.web.mapper;

import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.job.infrastructure.adapter.in.web.dto.JobPositionRequest;
import com.br.ats.consult.api.job.infrastructure.adapter.in.web.dto.UpdateJobPositionRequest;
import com.br.ats.consult.api.job.infrastructure.adapter.in.web.dto.JobPositionResponse;
import com.br.ats.consult.api.job.infrastructure.adapter.in.web.dto.JobTechnologyResponse; // ◄— Importa o DTO local
import com.br.ats.consult.api.technology.domain.model.Technology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface JobPositionWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", expression = "java(toClientRef(request.clientId()))")
    @Mapping(target = "technologies", expression = "java(toTechRefs(request.technologyIds()))")
    JobPosition toDomain(JobPositionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", expression = "java(toClientRef(request.clientId()))")
    @Mapping(target = "technologies", expression = "java(toTechRefs(request.technologyIds()))")
    JobPosition toDomain(UpdateJobPositionRequest request);

    JobPositionResponse toResponse(JobPosition domain);

    // ◄— Adicionado: Ensina o MapStruct a converter Technology para JobTechnologyResponse usando o fromDomain
    default JobTechnologyResponse mapTechnology(Technology technology) {
        return JobTechnologyResponse.fromDomain(technology);
    }

    default List<Technology> toTechRefs(List<Long> ids) {
        if (ids == null) return null;
        return ids.stream()
                .map(id -> Technology.builder().id(id).build())
                .toList();
    }

    default Client toClientRef(Long clientId) {
        return clientId == null ? null : Client.builder().id(clientId).build();
    }
}