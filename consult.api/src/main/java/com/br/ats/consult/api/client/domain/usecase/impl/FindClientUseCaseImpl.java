package com.br.ats.consult.api.client.domain.usecase.impl;

import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.client.domain.port.out.ClientGateway;
import com.br.ats.consult.api.client.domain.usecase.FindClientUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class FindClientUseCaseImpl implements FindClientUseCase {

    private final ClientGateway gateway;

    public FindClientUseCaseImpl(ClientGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findById(Long id) {
        return gateway.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> findAll() {
        return gateway.findAll();
    }
}
