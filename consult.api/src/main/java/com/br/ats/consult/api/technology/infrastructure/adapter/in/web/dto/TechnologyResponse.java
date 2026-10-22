package com.br.ats.consult.api.technology.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;

public record TechnologyResponse(
        Long id,
        String name,
        TechnologyCategoryEnum category,
        Long parentId
) {
    public static TechnologyResponse fromDomain(Technology domain) {
        return new TechnologyResponse(
                domain.getId(),
                domain.getName(),
                domain.getCategory(),
                domain.getParentId()
        );
    }
}