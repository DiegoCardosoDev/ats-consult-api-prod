package com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.mapper;

import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.model.CandidateAddress;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto.CandidateAddressRequest;
import com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto.CandidateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CandidateWebMapper {

    @Mapping(target = "id", ignore = true)
    Candidate toDomain(CandidateRequest request);

    CandidateAddress toDomain(CandidateAddressRequest request);
}
