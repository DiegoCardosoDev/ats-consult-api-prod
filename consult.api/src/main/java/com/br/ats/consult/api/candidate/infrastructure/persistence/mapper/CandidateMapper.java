package com.br.ats.consult.api.candidate.infrastructure.persistence.mapper;

import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.model.CandidateAddress;
import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateAddressEmbeddable;
import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateEntity;
import com.br.ats.consult.api.technology.domain.model.Technology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CandidateMapper {

    @Mapping(target = "technologies", ignore = true)
    Candidate toDomain(CandidateEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "technologyIds", expression = "java(mapTechnologiesToEntityIds(domain.getTechnologies()))")
    CandidateEntity toEntity(Candidate domain);

    CandidateAddress toDomain(CandidateAddressEmbeddable embeddable);

    CandidateAddressEmbeddable toEmbeddable(CandidateAddress address);

    default List<Long> mapTechnologiesToEntityIds(List<Technology> technologies) {
        if (technologies == null) {
            return List.of();
        }
        return technologies.stream()
                .map(Technology::getId)
                .toList();
    }
}
