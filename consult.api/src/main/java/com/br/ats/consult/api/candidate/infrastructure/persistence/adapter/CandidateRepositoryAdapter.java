package com.br.ats.consult.api.candidate.infrastructure.persistence.adapter;

import com.br.ats.consult.api.candidate.domain.exception.CandidateNotFoundException;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateEntity;
import com.br.ats.consult.api.candidate.infrastructure.persistence.mapper.CandidateMapper;
import com.br.ats.consult.api.candidate.infrastructure.persistence.repository.SpringDataCandidateRepository;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CandidateRepositoryAdapter implements CandidateGateway {

    private final SpringDataCandidateRepository repository;
    private final CandidateMapper mapper;
    private final TechnologyGateway technologyGateway;

    public CandidateRepositoryAdapter(SpringDataCandidateRepository repository, CandidateMapper mapper,
                                      TechnologyGateway technologyGateway) {
        this.repository = repository;
        this.mapper = mapper;
        this.technologyGateway = technologyGateway;
    }

    @Override
    public Candidate save(Candidate candidate) {
        CandidateEntity entity = candidate.getId() == null
                ? new CandidateEntity()
                : repository.findById(candidate.getId())
                .orElseThrow(() -> new CandidateNotFoundException(candidate.getId()));

        CandidateEntity mapped = mapper.toEntity(candidate);
        entity.setName(mapped.getName());
        entity.setAge(mapped.getAge());
        entity.setRole(mapped.getRole());
        entity.setLinkedinUrl(mapped.getLinkedinUrl());
        entity.setEmail(mapped.getEmail());
        entity.setPhone(mapped.getPhone());
        entity.setEducationLevel(mapped.getEducationLevel());
        entity.setEnglishLevel(mapped.getEnglishLevel());
        entity.setTechnologyIds(new ArrayList<>(mapped.getTechnologyIds()));
        entity.setAddress(mapped.getAddress());

        CandidateEntity saved = repository.save(entity);
        return toDomainList(List.of(saved)).get(0);
    }

    @Override
    public Optional<Candidate> findById(Long id) {
        return repository.findById(id).map(entity -> toDomainList(List.of(entity)).get(0));
    }

    @Override
    public Optional<Candidate> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email)
                .map(entity -> toDomainList(List.of(entity)).get(0));
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Page<Candidate> search(String name, String role, Pageable pageable) {
        Page<CandidateEntity> page;
        if (name != null && role != null) {
            page = repository.findByNameContainingIgnoreCaseAndRoleContainingIgnoreCase(name, role, pageable);
        } else if (name != null) {
            page = repository.findByNameContainingIgnoreCase(name, pageable);
        } else if (role != null) {
            page = repository.findByRoleContainingIgnoreCase(role, pageable);
        } else {
            page = repository.findAll(pageable);
        }

        return new PageImpl<>(
                toDomainList(page.getContent()),
                pageable,
                page.getTotalElements()
        );
    }

    private List<Candidate> toDomainList(List<CandidateEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        Set<Long> technologyIds = entities.stream()
                .map(CandidateEntity::getTechnologyIds)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<Long, Technology> technologiesById = loadTechnologies(technologyIds);

        return entities.stream()
                .map(entity -> toDomain(entity, technologiesById))
                .toList();
    }

    private Map<Long, Technology> loadTechnologies(Set<Long> technologyIds) {
        if (technologyIds.isEmpty()) {
            return Map.of();
        }
        return technologyGateway.findAllByIds(new ArrayList<>(technologyIds)).stream()
                .collect(Collectors.toMap(Technology::getId, Function.identity()));
    }

    private Candidate toDomain(CandidateEntity entity, Map<Long, Technology> technologiesById) {
        Candidate domain = mapper.toDomain(entity);
        List<Technology> technologies = entity.getTechnologyIds() == null
                ? List.of()
                : entity.getTechnologyIds().stream()
                .map(technologiesById::get)
                .filter(Objects::nonNull)
                .toList();
        domain.setTechnologies(new ArrayList<>(technologies));
        return domain;
    }
}
