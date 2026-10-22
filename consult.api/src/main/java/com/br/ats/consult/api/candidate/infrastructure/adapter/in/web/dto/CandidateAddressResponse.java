package com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.candidate.domain.model.CandidateAddress;

public record CandidateAddressResponse(
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        String country
) {

    public static CandidateAddressResponse fromDomain(CandidateAddress domain) {
        if (domain == null) {
            return null;
        }
        return new CandidateAddressResponse(
                domain.getStreet(),
                domain.getNumber(),
                domain.getComplement(),
                domain.getNeighborhood(),
                domain.getCity(),
                domain.getState(),
                domain.getZipCode(),
                domain.getCountry()
        );
    }
}
