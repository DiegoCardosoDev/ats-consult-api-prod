package br.com.ats.consult.api.client.infrastructure.adapter.in.web.dto;

import br.com.ats.consult.api.client.domain.model.Client;

public record ClientResponse(
        Long id,
        String name,
        String cnpj,
        String code
) {

    public static ClientResponse fromDomain(Client domain) {
        if (domain == null) {
            return null;
        }
        return new ClientResponse(
                domain.getId(),
                domain.getName(),
                Client.formatCnpj(domain.getCnpj()),
                domain.getCode()
        );
    }
}
