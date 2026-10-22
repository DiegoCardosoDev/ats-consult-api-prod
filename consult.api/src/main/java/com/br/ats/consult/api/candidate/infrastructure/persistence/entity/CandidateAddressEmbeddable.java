package com.br.ats.consult.api.candidate.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAddressEmbeddable {

    @Column(name = "address_street", length = 150)
    private String street;

    @Column(name = "address_number", length = 20)
    private String number;

    @Column(name = "address_complement", length = 100)
    private String complement;

    @Column(name = "address_neighborhood", length = 100)
    private String neighborhood;

    @Column(name = "address_city", length = 100)
    private String city;

    @Column(name = "address_state", length = 50)
    private String state;

    @Column(name = "address_zip_code", length = 20)
    private String zipCode;

    @Column(name = "address_country", length = 100)
    private String country;
}
