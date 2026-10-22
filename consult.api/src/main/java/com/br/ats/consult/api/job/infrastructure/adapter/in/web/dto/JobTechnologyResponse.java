package com.br.ats.consult.api.job.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.technology.domain.model.Technology;

public record JobTechnologyResponse(
        Long id,
        String name,
        String category, // Representado como String na camada web/DTO
        Long parentId
) {
    // Método auxiliar para converter do Domínio para este DTO local do módulo job
    public static JobTechnologyResponse fromDomain(Technology technology) {
        if (technology == null) {
            return null;
        }

        return new JobTechnologyResponse(
                technology.getId(),
                technology.getName(),
                technology.getCategory() != null ? technology.getCategory().name() : null, // Converte o Enum para String em segurança
                technology.getParentId()
        );
    }
}