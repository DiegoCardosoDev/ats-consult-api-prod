package com.br.ats.consult.api.job.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.job.domain.model.Location;

public record LocationResponse(
        String city,
        String state,
        String country
) {
    public static LocationResponse fromDomain(Location domain) {
        if (domain == null) return null;
        return new LocationResponse(
                domain.getCity(),
                domain.getState(),
                domain.getCountry()
        );
    }
}