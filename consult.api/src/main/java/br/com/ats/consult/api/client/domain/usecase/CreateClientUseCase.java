package br.com.ats.consult.api.client.domain.usecase;

import br.com.ats.consult.api.client.domain.model.Client;

public interface CreateClientUseCase {

    Client execute(Client client);
}
