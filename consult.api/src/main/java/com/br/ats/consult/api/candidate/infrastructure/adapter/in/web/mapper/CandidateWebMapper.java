package com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.mapper;

import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.model.CandidateAddress;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto.CandidateAddressRequest;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto.CandidateRequest;
import com.br.ats.consult.api.technology.domain.model.Technology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CandidateWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "technologies", expression = "java(toTechnologyRefs(request.technologyIds()))")
    Candidate toDomain(CandidateRequest request);

    CandidateAddress toDomain(CandidateAddressRequest request);

    default List<Technology> toTechnologyRefs(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream()
                .map(id -> Technology.builder().id(id).build())
                .toList();
    }
}
