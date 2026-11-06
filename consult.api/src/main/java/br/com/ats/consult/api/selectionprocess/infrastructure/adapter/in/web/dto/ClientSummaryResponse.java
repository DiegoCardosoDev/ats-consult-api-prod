package br.com.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import br.com.ats.consult.api.client.domain.model.Client;

public record ClientSummaryResponse(
        Long id,
        String name,
        String code
) {

    public static ClientSummaryResponse fromDomain(Client client) {
        if (client == null) {
            return null;
        }
        return new ClientSummaryResponse(client.getId(), client.getName(), client.getCode());
    }
}
