package com.br.ats.consult.api.job.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

public record LocationRequest(
        @Size(max = 100, message = "A cidade não pode exceder 100 caracteres")
        String city,
        @Size(max = 100, message = "O estado não pode exceder 100 caracteres")
        String state,
        @Size(max = 100, message = "O país não pode exceder 100 caracteres")
        String country
) {
}
