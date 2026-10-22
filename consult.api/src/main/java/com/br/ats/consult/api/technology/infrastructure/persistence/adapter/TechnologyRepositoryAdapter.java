package com.br.ats.consult.api.technology.infrastructure.persistence.adapter;

import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import com.br.ats.consult.api.technology.infrastructure.persistence.entity.TechnologyEntity;
import com.br.ats.consult.api.technology.infrastructure.persistence.mapper.TechnologyMapper;
import com.br.ats.consult.api.technology.infrastructure.persistence.repository.SpringDataTechnologyRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de persistência que implementa a porta de saída {@link TechnologyGateway},
 * fazendo a ponte entre o domínio e o Spring Data JPA sem expor detalhes de banco para o núcleo.
 */
public class TechnologyRepositoryAdapter implements TechnologyGateway {

    private final SpringDataTechnologyRepository repository;
    private final TechnologyMapper mapper;

    public TechnologyRepositoryAdapter(SpringDataTechnologyRepository repository, TechnologyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Technology save(Technology technology) {
        TechnologyEntity entity = mapper.toEntity(technology);
        TechnologyEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Technology> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Technology> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Technology> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findAllByIdIn(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByNameAndParent(String name, Long parentId) {
        return parentId == null
                ? repository.existsByNameIgnoreCaseAndParentIdIsNull(name)
                : repository.existsByNameIgnoreCaseAndParentId(name, parentId);
    }

    @Override
    public List<Technology> findByParentId(Long parentId) {
        return repository.findByParentId(parentId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}