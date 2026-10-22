package com.br.ats.consult.api.technology.domain.usecase.impl;

import com.br.ats.consult.api.technology.domain.exception.TechnologyNotFoundException;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import com.br.ats.consult.api.technology.domain.usecase.FindTechnologyUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

public class FindTechnologyUseCaseImpl implements FindTechnologyUseCase {

    private final TechnologyGateway technologyGateway;

    public FindTechnologyUseCaseImpl(TechnologyGateway technologyGateway) {
        this.technologyGateway = Objects.requireNonNull(technologyGateway, "TechnologyGateway não pode ser nulo");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Technology> findAll() {
        return technologyGateway.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Technology findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("O ID da tecnologia é obrigatório para a busca.");
        }
        return technologyGateway.findById(id)
                .orElseThrow(() -> new TechnologyNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Technology> findByParentId(Long parentId) {
        if (parentId == null) {
            throw new IllegalArgumentException("O ID pai não pode ser nulo.");
        }
        return technologyGateway.findByParentId(parentId);
    }
}