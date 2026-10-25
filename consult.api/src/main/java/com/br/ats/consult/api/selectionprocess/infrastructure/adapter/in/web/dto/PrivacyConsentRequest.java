package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PrivacyConsentRequest(
        @NotNull Boolean accepted,
        @NotBlank @Size(max = 50) String version
) {
}
