package com.br.ats.consult.api.client.domain.port.out;

import com.br.ats.consult.api.client.domain.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientGateway {

    Client save(Client client);

    Optional<Client> findById(Long id);

    Optional<Client> findByCode(String code);

    List<Client> findAll();

    boolean existsByCnpj(String cnpj);

    boolean existsByCode(String code);
}
