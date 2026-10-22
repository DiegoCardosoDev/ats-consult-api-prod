package com.br.ats.consult.api.candidate.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAddress {

    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    public void normalize() {
        street = trimToNull(street);
        number = trimToNull(number);
        complement = trimToNull(complement);
        neighborhood = trimToNull(neighborhood);
        city = trimToNull(city);
        state = trimToNull(state);
        zipCode = trimToNull(zipCode);
        country = trimToNull(country);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
