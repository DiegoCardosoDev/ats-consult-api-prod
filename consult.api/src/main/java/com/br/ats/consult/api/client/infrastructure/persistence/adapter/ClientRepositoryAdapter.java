package com.br.ats.consult.api.client.infrastructure.persistence.adapter;

import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.client.domain.port.out.ClientGateway;
import com.br.ats.consult.api.client.infrastructure.persistence.entity.ClientEntity;
import com.br.ats.consult.api.client.infrastructure.persistence.mapper.ClientMapper;
import com.br.ats.consult.api.client.infrastructure.persistence.repository.SpringDataClientRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ClientRepositoryAdapter implements ClientGateway {

    private final SpringDataClientRepository repository;
    private final ClientMapper mapper;

    public ClientRepositoryAdapter(SpringDataClientRepository repository, ClientMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Client save(Client client) {
        ClientEntity saved = repository.save(mapper.toEntity(client));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Client> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Client> findByCode(String code) {
        return repository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public List<Client> findAll() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCnpj(String cnpj) {
        return repository.existsByCnpj(cnpj);
    }

    @Override
    public boolean existsByCode(String code) {
        return repository.existsByCode(code);
    }
}
