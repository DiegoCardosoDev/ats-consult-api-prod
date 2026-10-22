package com.br.ats.consult.api.candidate.infrastructure.persistence.adapter;

import com.br.ats.consult.api.candidate.domain.exception.CandidateNotFoundException;
import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.candidate.infrastructure.persistence.entity.CandidateEntity;
import com.br.ats.consult.api.candidate.infrastructure.persistence.mapper.CandidateMapper;
import com.br.ats.consult.api.candidate.infrastructure.persistence.repository.SpringDataCandidateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CandidateRepositoryAdapter implements CandidateGateway {

    private final SpringDataCandidateRepository repository;
    private final CandidateMapper mapper;

    public CandidateRepositoryAdapter(SpringDataCandidateRepository repository, CandidateMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
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
        entity.setAddress(mapped.getAddress());

        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Candidate> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Candidate> findByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).map(mapper::toDomain);
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
                page.getContent().stream().map(mapper::toDomain).toList(),
                pageable,
                page.getTotalElements()
        );
    }
}
