package com.br.ats.consult.api.job.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.client.domain.model.Client;

public record JobClientResponse(
        Long id,
        String name,
        String code
) {

    public static JobClientResponse fromDomain(Client client) {
        if (client == null) {
            return null;
        }
        return new JobClientResponse(client.getId(), client.getName(), client.getCode());
    }
}
