package com.br.ats.consult.api.job.infrastructure.persistence.adapter;

import com.br.ats.consult.api.client.domain.exception.ClientNotFoundException;
import com.br.ats.consult.api.client.infrastructure.persistence.entity.ClientEntity;
import com.br.ats.consult.api.client.infrastructure.persistence.repository.SpringDataClientRepository;
import com.br.ats.consult.api.job.domain.exception.JobPositionNotFoundException;
import com.br.ats.consult.api.job.domain.model.JobPosition;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.job.infrastructure.persistence.entity.JobPositionEntity;
import com.br.ats.consult.api.job.infrastructure.persistence.mapper.JobPositionMapper;
import com.br.ats.consult.api.job.infrastructure.persistence.repository.SpringDataJobPositionRepository;
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
public class JobPositionRepositoryAdapter implements JobPositionGateway {

    private final SpringDataJobPositionRepository repository;
    private final SpringDataClientRepository clientRepository;
    private final JobPositionMapper mapper;
    private final TechnologyGateway technologyGateway;

    public JobPositionRepositoryAdapter(SpringDataJobPositionRepository repository,
                                        SpringDataClientRepository clientRepository,
                                        JobPositionMapper mapper,
                                        TechnologyGateway technologyGateway) {
        this.repository = repository;
        this.clientRepository = clientRepository;
        this.mapper = mapper;
        this.technologyGateway = technologyGateway;
    }

    @Override
    public JobPosition save(JobPosition jobPosition) {
        JobPositionEntity entity = mapper.toEntity(jobPosition);
        entity.setClient(resolveClient(jobPosition));
        JobPositionEntity savedEntity = repository.save(entity);
        return toDomainList(List.of(savedEntity)).get(0);
    }

    @Override
    public JobPosition update(Long id, JobPosition jobPosition) {
        JobPositionEntity entity = repository.findById(id)
                .orElseThrow(() -> new JobPositionNotFoundException(id));
        JobPositionEntity mapped = mapper.toEntity(jobPosition);

        entity.setClient(resolveClient(jobPosition));
        entity.setTitle(mapped.getTitle());
        entity.setDescription(mapped.getDescription());
        entity.setStatus(mapped.getStatus());
        entity.setWorkModel(mapped.getWorkModel());
        entity.setContractType(mapped.getContractType());
        entity.setEducationLevel(mapped.getEducationLevel());
        entity.setEnglishLevel(mapped.getEnglishLevel());
        entity.setMinSalary(mapped.getMinSalary());
        entity.setMaxSalary(mapped.getMaxSalary());
        entity.setLocation(mapped.getLocation());
        entity.setTechnologyIds(new ArrayList<>(mapped.getTechnologyIds()));

        JobPositionEntity savedEntity = repository.save(entity);
        return toDomainList(List.of(savedEntity)).get(0);
    }

    @Override
    public Page<JobPosition> findAll(Pageable pageable) {
        Page<JobPositionEntity> page = repository.findAll(pageable);
        return toDomainPage(page, pageable);
    }

    @Override
    public Optional<JobPosition> findById(Long id) {
        return repository.findById(id)
                .map(entity -> toDomainList(List.of(entity)).get(0));
    }

    @Override
    public Page<JobPosition> findByTechnologyId(Long technologyId, Pageable pageable) {
        Page<JobPositionEntity> page = repository.findByTechnologyId(technologyId, pageable);
        return toDomainPage(page, pageable);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private ClientEntity resolveClient(JobPosition jobPosition) {
        if (jobPosition.getClient() == null || jobPosition.getClient().getId() == null) {
            throw new IllegalArgumentException("O cliente da vaga é obrigatório.");
        }
        return clientRepository.findById(jobPosition.getClient().getId())
                .orElseThrow(() -> new ClientNotFoundException(jobPosition.getClient().getId()));
    }

    private Page<JobPosition> toDomainPage(Page<JobPositionEntity> page, Pageable pageable) {
        List<JobPosition> content = toDomainList(page.getContent());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    private List<JobPosition> toDomainList(List<JobPositionEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        Set<Long> technologyIds = entities.stream()
                .map(JobPositionEntity::getTechnologyIds)
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

    private JobPosition toDomain(JobPositionEntity entity, Map<Long, Technology> technologiesById) {
        JobPosition domain = mapper.toDomain(entity);
        List<Technology> technologies = entity.getTechnologyIds() == null
                ? List.of()
                : entity.getTechnologyIds().stream()
                .map(technologiesById::get)
                .filter(Objects::nonNull)
                .toList();
        domain.setTechnologies(technologies);
        return domain;
    }
}
