package com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

public record CandidateAddressRequest(
        @Size(max = 150, message = "A rua não pode exceder 150 caracteres")
        String street,
        @Size(max = 20, message = "O número não pode exceder 20 caracteres")
        String number,
        @Size(max = 100, message = "O complemento não pode exceder 100 caracteres")
        String complement,
        @Size(max = 100, message = "O bairro não pode exceder 100 caracteres")
        String neighborhood,
        @Size(max = 100, message = "A cidade não pode exceder 100 caracteres")
        String city,
        @Size(max = 50, message = "O estado não pode exceder 50 caracteres")
        String state,
        @Size(max = 20, message = "O código postal não pode exceder 20 caracteres")
        String zipCode,
        @Size(max = 100, message = "O país não pode exceder 100 caracteres")
        String country
) {
}
