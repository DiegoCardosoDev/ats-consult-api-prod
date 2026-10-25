package com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.technology.domain.model.Technology;

public record CandidateTechnologyResponse(
        Long id,
        String name,
        String category,
        Long parentId
) {

    public static CandidateTechnologyResponse fromDomain(Technology technology) {
        if (technology == null) {
            return null;
        }
        return new CandidateTechnologyResponse(
                technology.getId(),
                technology.getName(),
                technology.getCategory() == null ? null : technology.getCategory().name(),
                technology.getParentId()
        );
    }
}
