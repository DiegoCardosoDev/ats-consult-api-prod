package com.br.ats.consult.api.candidate.infrastructure.persistence.mapper;

import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.model.CandidateAddress;
import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateAddressEmbeddable;
import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CandidateMapper {

    Candidate toDomain(CandidateEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CandidateEntity toEntity(Candidate domain);

    CandidateAddress toDomain(CandidateAddressEmbeddable embeddable);

    CandidateAddressEmbeddable toEmbeddable(CandidateAddress address);
}
