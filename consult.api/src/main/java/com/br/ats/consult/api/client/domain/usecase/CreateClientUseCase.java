package com.br.ats.consult.api.client.domain.usecase;

import com.br.ats.consult.api.client.domain.model.Client;

public interface CreateClientUseCase {

    Client execute(Client client);
}
