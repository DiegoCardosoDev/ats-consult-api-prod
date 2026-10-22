package com.br.ats.consult.api.client.domain.usecase;

import com.br.ats.consult.api.client.domain.model.Client;

import java.util.List;
import java.util.Optional;

public interface FindClientUseCase {

    Optional<Client> findById(Long id);

    List<Client> findAll();
}
