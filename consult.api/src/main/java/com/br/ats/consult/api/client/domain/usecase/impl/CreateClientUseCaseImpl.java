package com.br.ats.consult.api.client.domain.usecase.impl;

import com.br.ats.consult.api.client.domain.exception.ClientAlreadyExistsException;
import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.client.domain.port.out.ClientGateway;
import com.br.ats.consult.api.client.domain.usecase.CreateClientUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

public class CreateClientUseCaseImpl implements CreateClientUseCase {

    private final ClientGateway gateway;

    public CreateClientUseCaseImpl(ClientGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    @Transactional
    public Client execute(Client client) {
        client.normalize();
        if (client.getCode() == null) {
            client.setCode(generateUniqueCode());
        }
        client.validate();

        if (gateway.existsByCnpj(client.getCnpj())) {
            throw new ClientAlreadyExistsException("Já existe um cliente com o CNPJ informado.");
        }
        if (gateway.existsByCode(client.getCode())) {
            throw new ClientAlreadyExistsException("Já existe um cliente com o código informado.");
        }

        return gateway.save(client);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = "CLI-" + UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase(Locale.ROOT);
        } while (gateway.existsByCode(code));
        return code;
    }
}
