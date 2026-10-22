package com.br.ats.consult.api.technology.domain.usecase.impl;

import com.br.ats.consult.api.technology.domain.exception.TechnologyAlreadyExistsException;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import com.br.ats.consult.api.technology.domain.usecase.CreateTechnologyUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Implementação do caso de uso responsável pela criação de uma nova tecnologia.
 */
public class CreateTechnologyUseCaseImpl implements CreateTechnologyUseCase {

    private final TechnologyGateway technologyGateway;

    public CreateTechnologyUseCaseImpl(TechnologyGateway technologyGateway) {
        this.technologyGateway = Objects.requireNonNull(technologyGateway, "TechnologyGateway não pode ser nulo");
    }

    @Override
    @Transactional
    public Technology execute(String name, TechnologyCategoryEnum category, Long parentId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da tecnologia é obrigatório.");
        }

        if (category == null) {
            throw new IllegalArgumentException("A categoria da tecnologia é obrigatória.");
        }

        String normalizedName = name.trim();
        if (technologyGateway.existsByNameAndParent(normalizedName, parentId)) {
            throw new TechnologyAlreadyExistsException(normalizedName, parentId);
        }

        Technology newTechnology = Technology.builder()
                .name(normalizedName)
                .category(category)
                .parentId(parentId)
                .build();

        return technologyGateway.save(newTechnology);
    }
}